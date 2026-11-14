package com.restaurante.menu.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.menu.dto.NuevoPedidoRequest;
import com.restaurante.menu.dto.PedidoCompleto;
import com.restaurante.menu.entity.Adicional;
import com.restaurante.menu.entity.DetallePedido;
import com.restaurante.menu.entity.Mesa;
import com.restaurante.menu.entity.Pedido;
import com.restaurante.menu.entity.Producto;
import com.restaurante.menu.entity.VarianteProducto;

/**
 * Acceso a pedidos, detalles y adicionales. Al crear un pedido, los precios y el total se
 * calculan aquí con los datos de la base, nunca con valores enviados por el cliente.
 */
@Repository
public class PedidoRepository {

	private static final String TIPO_MESA = "MESA";
	private static final String TIPO_DOMICILIO = "DOMICILIO";
	private static final Set<String> TIPOS = Set.of(TIPO_MESA, "LLEVAR", TIPO_DOMICILIO);

	private static final int CANTIDAD_MAXIMA = 50;

	/** Estados permitidos y a cuáles se puede pasar desde cada uno. */
	private static final Map<String, Set<String>> TRANSICIONES = Map.of(
			"RECIBIDO", Set.of("EN_PREPARACION", "CANCELADO"),
			"EN_PREPARACION", Set.of("LISTO", "CANCELADO"),
			"LISTO", Set.of("ENTREGADO", "CANCELADO"),
			"ENTREGADO", Set.of(),
			"CANCELADO", Set.of());

	private static final String SELECT_PEDIDO =
			"SELECT id, mesa_id, tipo_pedido, nombre_cliente, telefono, direccion, fecha_hora, "
					+ "estado, total, observaciones FROM pedido";

	private static final RowMapper<Pedido> PEDIDO_MAPPER = (rs, fila) -> new Pedido(
			rs.getInt("id"),
			RepositoryUtil.enteroONulo(rs, "mesa_id"),
			rs.getString("tipo_pedido"),
			rs.getString("nombre_cliente"),
			rs.getString("telefono"),
			rs.getString("direccion"),
			rs.getTimestamp("fecha_hora").toLocalDateTime(),
			rs.getString("estado"),
			rs.getBigDecimal("total"),
			rs.getString("observaciones"));

	private final JdbcTemplate jdbc;
	private final MesaRepository mesaRepository;
	private final ProductoRepository productoRepository;
	private final VarianteProductoRepository varianteRepository;
	private final AdicionalRepository adicionalRepository;

	public PedidoRepository(JdbcTemplate jdbc, MesaRepository mesaRepository, ProductoRepository productoRepository,
			VarianteProductoRepository varianteRepository, AdicionalRepository adicionalRepository) {
		this.jdbc = jdbc;
		this.mesaRepository = mesaRepository;
		this.productoRepository = productoRepository;
		this.varianteRepository = varianteRepository;
		this.adicionalRepository = adicionalRepository;
	}

	// ------------------------------------------------------------------
	// Creación
	// ------------------------------------------------------------------

	/**
	 * Registra el pedido completo (pedido, detalles y adicionales) en una sola transacción.
	 *
	 * @throws IllegalArgumentException si los datos del pedido no son válidos
	 */
	@Transactional
	public PedidoCompleto crear(NuevoPedidoRequest solicitud) {
		if (solicitud == null || solicitud.items() == null || solicitud.items().isEmpty()) {
			throw new IllegalArgumentException("El pedido debe tener al menos un producto");
		}

		String tipo = solicitud.tipoPedido() == null || solicitud.tipoPedido().isBlank()
				? TIPO_MESA
				: solicitud.tipoPedido().trim().toUpperCase();
		if (!TIPOS.contains(tipo)) {
			throw new IllegalArgumentException("Tipo de pedido no válido: " + solicitud.tipoPedido());
		}

		String nombreCliente = limpiar(solicitud.nombreCliente(), "nombreCliente", 100);
		String telefono = limpiar(solicitud.telefono(), "telefono", 20);
		String direccion = limpiar(solicitud.direccion(), "direccion", 200);
		String observaciones = limpiar(solicitud.observaciones(), "observaciones", 300);

		Integer mesaId = null;
		if (TIPO_MESA.equals(tipo)) {
			mesaId = resolverMesa(solicitud.mesaCodigo()).getId();
		} else if (TIPO_DOMICILIO.equals(tipo) && (telefono == null || direccion == null)) {
			throw new IllegalArgumentException("Un domicilio requiere teléfono y dirección");
		}

		final Integer mesaFinal = mesaId;
		int pedidoId = RepositoryUtil.insertarConId(jdbc,
				"INSERT INTO pedido (mesa_id, tipo_pedido, nombre_cliente, telefono, direccion, "
						+ "estado, total, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
				ps -> {
					RepositoryUtil.asignarEntero(ps, 1, mesaFinal);
					ps.setString(2, tipo);
					ps.setString(3, nombreCliente);
					ps.setString(4, telefono);
					ps.setString(5, direccion);
					ps.setString(6, "RECIBIDO");
					ps.setBigDecimal(7, BigDecimal.ZERO);
					ps.setString(8, observaciones);
				});

		BigDecimal total = BigDecimal.ZERO;
		for (NuevoPedidoRequest.Item item : solicitud.items()) {
			total = total.add(insertarItem(pedidoId, item));
		}
		jdbc.update("UPDATE pedido SET total = ? WHERE id = ?", total, pedidoId);

		return buscarCompleto(pedidoId)
				.orElseThrow(() -> new IllegalStateException("No se pudo leer el pedido recién creado"));
	}

	private Mesa resolverMesa(String codigo) {
		if (codigo == null || codigo.isBlank()) {
			throw new IllegalArgumentException("Falta el código o número de la mesa");
		}
		String clean = codigo.trim();
		Optional<Mesa> mesa = mesaRepository.buscarPorCodigo(clean);
		if (mesa.isEmpty()) {
			try {
				int numero = Integer.parseInt(clean);
				mesa = mesaRepository.buscarPorNumero(numero);
			} catch (NumberFormatException ignored) {
			}
		}
		return mesa.filter(m -> Boolean.TRUE.equals(m.getActiva()))
				.orElseThrow(() -> new IllegalArgumentException("La mesa '" + clean + "' no existe o no está activa"));
	}

	/**
	 * Inserta un producto del pedido con sus adicionales y devuelve el subtotal de la línea.
	 */
	private BigDecimal insertarItem(int pedidoId, NuevoPedidoRequest.Item item) {
		if (item == null || item.productoId() == null) {
			throw new IllegalArgumentException("Cada producto del pedido debe indicar su id");
		}
		int cantidad = item.cantidad() == null ? 1 : item.cantidad();
		if (cantidad < 1 || cantidad > CANTIDAD_MAXIMA) {
			throw new IllegalArgumentException("La cantidad debe estar entre 1 y " + CANTIDAD_MAXIMA);
		}

		Producto producto = productoRepository.buscarPorId(item.productoId())
				.filter(p -> Boolean.TRUE.equals(p.getDisponible()))
				.orElseThrow(() -> new IllegalArgumentException(
						"El producto " + item.productoId() + " no existe o no está disponible"));

		final Integer varianteId = item.varianteId();
		final BigDecimal precioBase = precioBase(producto, varianteId);
		final String observacion = limpiar(item.observacion(), "observacion", 200);

		int detalleId = RepositoryUtil.insertarConId(jdbc,
				"INSERT INTO detalle_pedido (pedido_id, producto_id, variante_id, cantidad, "
						+ "precio_unitario, observacion) VALUES (?, ?, ?, ?, ?, ?)",
				ps -> {
					ps.setInt(1, pedidoId);
					ps.setInt(2, producto.getId());
					RepositoryUtil.asignarEntero(ps, 3, varianteId);
					ps.setInt(4, cantidad);
					ps.setBigDecimal(5, precioBase);
					ps.setString(6, observacion);
				});

		BigDecimal extras = BigDecimal.ZERO;
		if (item.adicionalIds() != null) {
			for (Integer adicionalId : new LinkedHashSet<>(item.adicionalIds())) {
				Adicional adicional = adicionalRepository.buscarPorId(adicionalId)
						.filter(a -> Boolean.TRUE.equals(a.getDisponible())
								&& (a.getCategoriaId() == null
										|| a.getCategoriaId().equals(producto.getCategoriaId())))
						.orElseThrow(() -> new IllegalArgumentException(
								"El adicional " + adicionalId + " no existe, no está disponible "
										+ "o no aplica a " + producto.getNombre()));
				jdbc.update("INSERT INTO detalle_pedido_adicional (detalle_pedido_id, adicional_id, precio) "
						+ "VALUES (?, ?, ?)", detalleId, adicional.getId(), adicional.getPrecio());
				extras = extras.add(adicional.getPrecio());
			}
		}

		return precioBase.add(extras).multiply(BigDecimal.valueOf(cantidad));
	}

	/**
	 * Precio del producto o de la variante elegida. Si el producto tiene variantes
	 * disponibles, el cliente debe elegir una.
	 */
	private BigDecimal precioBase(Producto producto, Integer varianteId) {
		if (varianteId != null) {
			VarianteProducto variante = varianteRepository.buscarPorId(varianteId)
					.filter(v -> v.getProductoId().equals(producto.getId())
							&& Boolean.TRUE.equals(v.getDisponible()))
					.orElseThrow(() -> new IllegalArgumentException(
							"La opción " + varianteId + " no es válida para " + producto.getNombre()));
			return variante.getPrecio();
		}
		if (varianteRepository.tieneVariantesDisponibles(producto.getId())) {
			throw new IllegalArgumentException("Debes elegir una opción para " + producto.getNombre());
		}
		return producto.getPrecio();
	}

	// ------------------------------------------------------------------
	// Consultas
	// ------------------------------------------------------------------

	public Optional<Pedido> buscarPorId(Integer id) {
		return jdbc.query(SELECT_PEDIDO + " WHERE id = ?", PEDIDO_MAPPER, id).stream().findFirst();
	}

	public Optional<PedidoCompleto> buscarCompleto(Integer id) {
		return buscarPorId(id).map(this::armarCompleto);
	}

	/**
	 * Pedidos que el personal todavía debe atender (recibidos, en preparación o listos),
	 * del más antiguo al más reciente.
	 */
	public List<PedidoCompleto> listarActivos() {
		return jdbc.query(SELECT_PEDIDO + " WHERE estado IN ('RECIBIDO', 'EN_PREPARACION', 'LISTO') "
				+ "ORDER BY fecha_hora, id", PEDIDO_MAPPER).stream().map(this::armarCompleto).toList();
	}

	public List<PedidoCompleto> listarPorEstado(String estado) {
		String filtro = estado == null ? "" : estado.trim().toUpperCase();
		if (!TRANSICIONES.containsKey(filtro)) {
			throw new IllegalArgumentException("Estado no válido: " + estado);
		}
		return jdbc.query(SELECT_PEDIDO + " WHERE estado = ? ORDER BY fecha_hora, id",
				PEDIDO_MAPPER, filtro).stream().map(this::armarCompleto).toList();
	}

	/**
	 * Pedidos registrados dentro de un rango [inicio, fin), del más reciente al más antiguo.
	 * Se usa en los reportes de ventas del panel.
	 */
	public List<PedidoCompleto> listarEntre(LocalDateTime inicio, LocalDateTime fin, int limite) {
		return jdbc.query(SELECT_PEDIDO
						+ " WHERE fecha_hora >= ? AND fecha_hora < ? "
						+ "ORDER BY fecha_hora DESC, id DESC LIMIT ?",
				PEDIDO_MAPPER, Timestamp.valueOf(inicio), Timestamp.valueOf(fin), limite)
				.stream().map(this::armarCompleto).toList();
	}

	private PedidoCompleto armarCompleto(Pedido pedido) {
		Integer mesaNumero = pedido.getMesaId() == null
				? null
				: mesaRepository.buscarPorId(pedido.getMesaId()).map(Mesa::getNumero).orElse(null);

		Map<Integer, List<PedidoCompleto.AdicionalLinea>> adicionales = new HashMap<>();
		jdbc.query("SELECT x.detalle_pedido_id, x.adicional_id, x.precio, a.nombre "
				+ "FROM detalle_pedido_adicional x "
				+ "JOIN adicional a ON a.id = x.adicional_id "
				+ "JOIN detalle_pedido d ON d.id = x.detalle_pedido_id "
				+ "WHERE d.pedido_id = ? ORDER BY x.id",
				(RowCallbackHandler) rs -> adicionales
						.computeIfAbsent(rs.getInt("detalle_pedido_id"), k -> new ArrayList<>())
						.add(new PedidoCompleto.AdicionalLinea(
								rs.getInt("adicional_id"), rs.getString("nombre"), rs.getBigDecimal("precio"))),
				pedido.getId());

		List<PedidoCompleto.Linea> lineas = jdbc.query(
				"SELECT d.id, d.pedido_id, d.producto_id, d.variante_id, d.cantidad, d.precio_unitario, "
						+ "d.observacion, p.nombre AS producto_nombre, v.nombre AS variante_nombre "
						+ "FROM detalle_pedido d "
						+ "JOIN producto p ON p.id = d.producto_id "
						+ "LEFT JOIN variante_producto v ON v.id = d.variante_id "
						+ "WHERE d.pedido_id = ? ORDER BY d.id",
				(rs, fila) -> {
					DetallePedido detalle = new DetallePedido(
							rs.getInt("id"),
							rs.getInt("pedido_id"),
							rs.getInt("producto_id"),
							RepositoryUtil.enteroONulo(rs, "variante_id"),
							rs.getInt("cantidad"),
							rs.getBigDecimal("precio_unitario"),
							rs.getString("observacion"));
					return new PedidoCompleto.Linea(
							detalle,
							rs.getString("producto_nombre"),
							rs.getString("variante_nombre"),
							adicionales.getOrDefault(detalle.getId(), List.of()));
				},
				pedido.getId());

		return new PedidoCompleto(pedido, mesaNumero, lineas);
	}

	// ------------------------------------------------------------------
	// Cambio de estado
	// ------------------------------------------------------------------

	/**
	 * Cambia el estado respetando el flujo RECIBIDO, EN_PREPARACION, LISTO, ENTREGADO
	 * (o CANCELADO mientras no esté entregado).
	 *
	 * @throws NoSuchElementException si el pedido no existe
	 * @throws IllegalArgumentException si el estado no existe
	 * @throws IllegalStateException si el cambio no está permitido desde el estado actual
	 */
	@Transactional
	public Pedido cambiarEstado(Integer id, String nuevoEstado) {
		String destino = nuevoEstado == null ? "" : nuevoEstado.trim().toUpperCase();
		if (!TRANSICIONES.containsKey(destino)) {
			throw new IllegalArgumentException("Estado no válido: " + nuevoEstado);
		}
		Pedido pedido = jdbc.query(SELECT_PEDIDO + " WHERE id = ? FOR UPDATE", PEDIDO_MAPPER, id)
				.stream().findFirst()
				.orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
		if (!TRANSICIONES.get(pedido.getEstado()).contains(destino)) {
			throw new IllegalStateException(
					"No se puede pasar el pedido de " + pedido.getEstado() + " a " + destino);
		}
		jdbc.update("UPDATE pedido SET estado = ? WHERE id = ?", destino, id);
		pedido.setEstado(destino);
		return pedido;
	}

	// ------------------------------------------------------------------

	/**
	 * Devuelve el texto sin espacios sobrantes, null si está vacío, y valida su longitud máxima.
	 */
	private static String limpiar(String valor, String campo, int maximo) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		String texto = valor.trim();
		if (texto.length() > maximo) {
			throw new IllegalArgumentException(
					"El campo '" + campo + "' no puede tener más de " + maximo + " caracteres");
		}
		return texto;
	}
}
