package com.restaurante.menu.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.restaurante.menu.dto.PedidoCompleto;
import com.restaurante.menu.dto.ReporteVentas;
import com.restaurante.menu.dto.ReporteVentas.PuntoSerie;
import com.restaurante.menu.repository.PedidoRepository;
import com.restaurante.menu.repository.ReporteRepository;
import com.restaurante.menu.repository.ReporteRepository.FilaAgrupada;
import com.restaurante.menu.repository.ReporteRepository.FilaProducto;
import com.restaurante.menu.repository.ReporteRepository.Resumen;

/**
 * Calcula el resumen de ventas de un día, un mes o un año y lo compara con el periodo anterior.
 */
@Service
public class ReporteService {

	private static final Locale ESPANOL = Locale.forLanguageTag("es-CO");

	private static final DateTimeFormatter TITULO_DIA =
			DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", ESPANOL);
	private static final DateTimeFormatter TITULO_MES =
			DateTimeFormatter.ofPattern("MMMM 'de' yyyy", ESPANOL);
	private static final DateTimeFormatter RANGO_MES =
			DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", ESPANOL);
	private static final DateTimeFormatter RANGO_ANIO =
			DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", ESPANOL);

	private static final String[] MESES_CORTOS = {
			"Ene", "Feb", "Mar", "Abr", "May", "Jun",
			"Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };

	private static final int TOP_PRODUCTOS = 8;
	private static final int PEDIDOS_RECIENTES = 12;

	private final ReporteRepository reporteRepository;
	private final PedidoRepository pedidoRepository;

	public ReporteService(ReporteRepository reporteRepository, PedidoRepository pedidoRepository) {
		this.reporteRepository = reporteRepository;
		this.pedidoRepository = pedidoRepository;
	}

	/**
	 * Genera el reporte del periodo indicado.
	 *
	 * @param periodo dia, mes o anio; cualquier otro valor usa dia
	 * @param fecha   fecha de referencia en formato ISO (yyyy-MM-dd); vacía usa hoy
	 */
	public ReporteVentas generar(String periodo, String fecha) {
		String tipo = normalizarPeriodo(periodo);
		LocalDate referencia = parsearFecha(fecha);

		LocalDateTime inicio;
		LocalDateTime fin;
		LocalDateTime inicioAnterior;
		String agrupacion;

		switch (tipo) {
			case "mes" -> {
				inicio = referencia.withDayOfMonth(1).atStartOfDay();
				fin = inicio.plusMonths(1);
				inicioAnterior = inicio.minusMonths(1);
				agrupacion = "dia";
			}
			case "anio" -> {
				inicio = LocalDate.of(referencia.getYear(), 1, 1).atStartOfDay();
				fin = inicio.plusYears(1);
				inicioAnterior = inicio.minusYears(1);
				agrupacion = "mes";
			}
			default -> {
				tipo = "dia";
				inicio = referencia.atStartOfDay();
				fin = inicio.plusDays(1);
				inicioAnterior = inicio.minusDays(1);
				agrupacion = "hora";
			}
		}

		Resumen actual = reporteRepository.resumen(inicio, fin);
		Resumen anterior = reporteRepository.resumen(inicioAnterior, inicio);
		int productos = reporteRepository.productosVendidos(inicio, fin);
		int productosAnterior = reporteRepository.productosVendidos(inicioAnterior, inicio);

		BigDecimal ticket = ticketPromedio(actual.ventas(), actual.pedidos());
		BigDecimal ticketAnterior = ticketPromedio(anterior.ventas(), anterior.pedidos());

		List<PuntoSerie> serie = construirSerie(tipo, referencia, reporteRepository.agrupar(inicio, fin, agrupacion));
		List<PuntoSerie> actividad = serie.stream().filter(punto -> punto.pedidos() > 0).toList();
		List<ReporteVentas.ProductoVendido> topProductos = construirTop(
				reporteRepository.topProductos(inicio, fin, TOP_PRODUCTOS));
		List<PedidoCompleto> recientes = pedidoRepository.listarEntre(inicio, fin, PEDIDOS_RECIENTES);

		return new ReporteVentas(
				tipo,
				titulo(tipo, referencia),
				rango(tipo, inicio, fin),
				referencia.toString(),
				desplazar(tipo, referencia, -1).toString(),
				desplazar(tipo, referencia, 1).toString(),
				unidad(tipo),
				actual.pedidos(),
				actual.cancelados(),
				actual.ventas(),
				ticket,
				productos,
				serie.stream().map(PuntoSerie::total).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO),
				variacion(actual.pedidos(), anterior.pedidos()),
				variacion(actual.ventas(), anterior.ventas()),
				variacion(ticket, ticketAnterior),
				variacion(productos, productosAnterior),
				serie,
				actividad,
				topProductos,
				recientes);
	}

	private String normalizarPeriodo(String periodo) {
		if (periodo == null) {
			return "dia";
		}
		String limpio = periodo.trim().toLowerCase(Locale.ROOT);
		return "mes".equals(limpio) || "anio".equals(limpio) ? limpio : "dia";
	}

	private LocalDate parsearFecha(String fecha) {
		if (fecha == null || fecha.isBlank()) {
			return LocalDate.now();
		}
		try {
			return LocalDate.parse(fecha.trim());
		} catch (DateTimeParseException exception) {
			return LocalDate.now();
		}
	}

	private LocalDate desplazar(String tipo, LocalDate fecha, int cantidad) {
		return switch (tipo) {
			case "mes" -> fecha.plusMonths(cantidad);
			case "anio" -> fecha.plusYears(cantidad);
			default -> fecha.plusDays(cantidad);
		};
	}

	private String titulo(String tipo, LocalDate referencia) {
		return switch (tipo) {
			case "mes" -> capitalizar(TITULO_MES.format(referencia));
			case "anio" -> "Año " + referencia.getYear();
			default -> capitalizar(TITULO_DIA.format(referencia));
		};
	}

	private String rango(String tipo, LocalDateTime inicio, LocalDateTime fin) {
		return switch (tipo) {
			case "mes" -> "1 – " + RANGO_MES.format(fin.minusDays(1).toLocalDate());
			case "anio" -> RANGO_ANIO.format(inicio.toLocalDate()) + " – "
					+ RANGO_ANIO.format(fin.minusDays(1).toLocalDate());
			default -> "00:00 – 23:59";
		};
	}

	private String unidad(String tipo) {
		return switch (tipo) {
			case "mes" -> "día";
			case "anio" -> "mes";
			default -> "hora";
		};
	}

	private BigDecimal ticketPromedio(BigDecimal ventas, int pedidos) {
		if (pedidos == 0 || ventas == null) {
			return BigDecimal.ZERO;
		}
		return ventas.divide(BigDecimal.valueOf(pedidos), 0, RoundingMode.HALF_UP);
	}

	private List<PuntoSerie> construirSerie(String tipo, LocalDate referencia, List<FilaAgrupada> filas) {
		Map<Integer, FilaAgrupada> porClave = filas.stream()
				.collect(Collectors.toMap(FilaAgrupada::clave, Function.identity(), (a, b) -> a));
		BigDecimal maximo = filas.stream()
				.map(FilaAgrupada::total)
				.max(Comparator.naturalOrder())
				.orElse(BigDecimal.ZERO);

		List<PuntoSerie> puntos = new ArrayList<>();
		if ("mes".equals(tipo)) {
			int dias = referencia.lengthOfMonth();
			for (int dia = 1; dia <= dias; dia++) {
				FilaAgrupada fila = porClave.get(dia);
				puntos.add(punto(String.valueOf(dia), fila, maximo,
						dia == 1 || dia % 5 == 0 || dia == dias));
			}
		} else if ("anio".equals(tipo)) {
			for (int mes = 1; mes <= 12; mes++) {
				puntos.add(punto(MESES_CORTOS[mes - 1], porClave.get(mes), maximo, true));
			}
		} else {
			for (int hora = 0; hora < 24; hora++) {
				puntos.add(punto(String.format("%02d", hora), porClave.get(hora), maximo, true));
			}
		}
		return puntos;
	}

	private PuntoSerie punto(String etiqueta, FilaAgrupada fila, BigDecimal maximo,
			boolean etiquetaVisible) {
		int pedidos = fila == null ? 0 : fila.pedidos();
		BigDecimal total = fila == null ? BigDecimal.ZERO : fila.total();
		return new PuntoSerie(etiqueta, pedidos, total, porcentaje(total, maximo), etiquetaVisible);
	}

	private List<ReporteVentas.ProductoVendido> construirTop(List<FilaProducto> filas) {
		int maximo = filas.stream().mapToInt(FilaProducto::cantidad).max().orElse(0);
		return filas.stream()
				.map(fila -> new ReporteVentas.ProductoVendido(
						fila.nombre(), fila.cantidad(), fila.total(), porcentaje(fila.cantidad(), maximo)))
				.toList();
	}

	private int porcentaje(BigDecimal valor, BigDecimal maximo) {
		if (valor == null || maximo == null || maximo.signum() <= 0 || valor.signum() <= 0) {
			return 0;
		}
		int porcentaje = valor.multiply(BigDecimal.valueOf(100))
				.divide(maximo, 0, RoundingMode.HALF_UP)
				.intValue();
		return Math.max(3, porcentaje);
	}

	private int porcentaje(int valor, int maximo) {
		if (valor <= 0 || maximo <= 0) {
			return 0;
		}
		return Math.max(4, (int) Math.round(valor * 100.0 / maximo));
	}

	private ReporteVentas.Variacion variacion(BigDecimal actual, BigDecimal anterior) {
		BigDecimal referencia = anterior == null ? BigDecimal.ZERO : anterior;
		BigDecimal valorActual = actual == null ? BigDecimal.ZERO : actual;
		int comparacion = valorActual.compareTo(referencia);

		if (referencia.signum() == 0) {
			return valorActual.signum() == 0
					? new ReporteVentas.Variacion("sin cambios", "flat")
					: new ReporteVentas.Variacion("nuevo", "new");
		}

		String tendencia = comparacion > 0 ? "up" : comparacion < 0 ? "down" : "flat";
		BigDecimal porcentaje = valorActual.subtract(referencia)
				.multiply(BigDecimal.valueOf(100))
				.divide(referencia, 1, RoundingMode.HALF_UP);

		NumberFormat formato = NumberFormat.getNumberInstance(ESPANOL);
		formato.setMinimumFractionDigits(0);
		formato.setMaximumFractionDigits(1);
		String texto = formato.format(porcentaje);
		if (porcentaje.signum() > 0) {
			texto = "+" + texto;
		}
		return new ReporteVentas.Variacion(texto + " %", tendencia);
	}

	private ReporteVentas.Variacion variacion(int actual, int anterior) {
		return variacion(BigDecimal.valueOf(actual), BigDecimal.valueOf(anterior));
	}

	private String capitalizar(String texto) {
		if (texto == null || texto.isEmpty()) {
			return texto;
		}
		return texto.substring(0, 1).toUpperCase(ESPANOL) + texto.substring(1);
	}
}
