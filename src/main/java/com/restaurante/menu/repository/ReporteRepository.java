package com.restaurante.menu.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consultas de solo lectura para los reportes de ventas.
 * Las fechas siempre llegan como rangos [inicio, fin) para aprovechar el índice de fecha_hora.
 */
@Repository
public class ReporteRepository {

	private static final Map<String, String> AGRUPACIONES = Map.of(
			"hora", "HOUR(fecha_hora)",
			"dia", "DAYOFMONTH(fecha_hora)",
			"mes", "MONTH(fecha_hora)");

	private final JdbcTemplate jdbc;

	public ReporteRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	/** Pedidos, cancelados e ingresos del rango. Los cancelados no suman dinero. */
	public Resumen resumen(LocalDateTime inicio, LocalDateTime fin) {
		return jdbc.queryForObject(
				"SELECT COALESCE(SUM(CASE WHEN estado <> 'CANCELADO' THEN 1 ELSE 0 END), 0) AS pedidos, "
						+ "COALESCE(SUM(CASE WHEN estado = 'CANCELADO' THEN 1 ELSE 0 END), 0) AS cancelados, "
						+ "COALESCE(SUM(CASE WHEN estado <> 'CANCELADO' THEN total ELSE 0 END), 0) AS ventas "
						+ "FROM pedido WHERE fecha_hora >= ? AND fecha_hora < ?",
				(rs, fila) -> new Resumen(
						rs.getInt("pedidos"), rs.getInt("cancelados"), rs.getBigDecimal("ventas")),
				Timestamp.valueOf(inicio), Timestamp.valueOf(fin));
	}

	/** Total de unidades vendidas (cantidad de platos), sin contar pedidos cancelados. */
	public int productosVendidos(LocalDateTime inicio, LocalDateTime fin) {
		Integer total = jdbc.queryForObject(
				"SELECT COALESCE(SUM(d.cantidad), 0) FROM detalle_pedido d "
						+ "JOIN pedido p ON p.id = d.pedido_id "
						+ "WHERE p.estado <> 'CANCELADO' AND p.fecha_hora >= ? AND p.fecha_hora < ?",
				Integer.class, Timestamp.valueOf(inicio), Timestamp.valueOf(fin));
		return total == null ? 0 : total;
	}

	/**
	 * Agrupa pedidos e ingresos por hora, día del mes o mes.
	 *
	 * @param agrupacion hora, dia o mes
	 */
	public List<FilaAgrupada> agrupar(LocalDateTime inicio, LocalDateTime fin, String agrupacion) {
		String expresion = AGRUPACIONES.get(agrupacion);
		if (expresion == null) {
			throw new IllegalArgumentException("Agrupación no válida: " + agrupacion);
		}
		return jdbc.query(
				"SELECT " + expresion + " AS clave, COUNT(*) AS pedidos, "
						+ "COALESCE(SUM(total), 0) AS total "
						+ "FROM pedido WHERE estado <> 'CANCELADO' "
						+ "AND fecha_hora >= ? AND fecha_hora < ? "
						+ "GROUP BY " + expresion + " ORDER BY " + expresion,
				(rs, fila) -> new FilaAgrupada(
						rs.getInt("clave"), rs.getInt("pedidos"), rs.getBigDecimal("total")),
				Timestamp.valueOf(inicio), Timestamp.valueOf(fin));
	}

	/** Ranking de productos por unidades, incluyendo el valor de sus adicionales. */
	public List<FilaProducto> topProductos(LocalDateTime inicio, LocalDateTime fin, int limite) {
		return jdbc.query(
				"SELECT pr.nombre AS nombre, SUM(d.cantidad) AS cantidad, "
						+ "SUM(d.cantidad * (d.precio_unitario + COALESCE(a.extras, 0))) AS total "
						+ "FROM detalle_pedido d "
						+ "JOIN pedido p ON p.id = d.pedido_id "
						+ "JOIN producto pr ON pr.id = d.producto_id "
						+ "LEFT JOIN (SELECT detalle_pedido_id, SUM(precio) AS extras "
						+ "FROM detalle_pedido_adicional GROUP BY detalle_pedido_id) a "
						+ "ON a.detalle_pedido_id = d.id "
						+ "WHERE p.estado <> 'CANCELADO' AND p.fecha_hora >= ? AND p.fecha_hora < ? "
						+ "GROUP BY pr.id, pr.nombre ORDER BY cantidad DESC, total DESC LIMIT ?",
				(rs, fila) -> new FilaProducto(
						rs.getString("nombre"), rs.getInt("cantidad"), rs.getBigDecimal("total")),
				Timestamp.valueOf(inicio), Timestamp.valueOf(fin), limite);
	}

	public record Resumen(int pedidos, int cancelados, BigDecimal ventas) {
	}

	public record FilaAgrupada(int clave, int pedidos, BigDecimal total) {
	}

	public record FilaProducto(String nombre, int cantidad, BigDecimal total) {
	}
}
