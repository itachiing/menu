package com.restaurante.menu.model.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.restaurante.menu.vo.VarianteProducto;

@Repository
public class VarianteProductoDAO {

	private static final String SELECT =
			"SELECT id, producto_id, nombre, precio, disponible FROM variante_producto";

	private static final RowMapper<VarianteProducto> MAPPER = (rs, fila) -> new VarianteProducto(
			rs.getInt("id"),
			rs.getInt("producto_id"),
			rs.getString("nombre"),
			rs.getBigDecimal("precio"),
			rs.getBoolean("disponible"));

	private final JdbcTemplate jdbc;

	public VarianteProductoDAO(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public List<VarianteProducto> listarPorProducto(Integer productoId, boolean soloDisponibles) {
		String sql = SELECT + " WHERE producto_id = ?" + (soloDisponibles ? " AND disponible = 1" : "")
				+ " ORDER BY precio, id";
		return jdbc.query(sql, MAPPER, productoId);
	}

	/**
	 * Todas las variantes disponibles (se usa para armar el menú completo con pocas consultas).
	 */
	public List<VarianteProducto> listarDisponibles() {
		return jdbc.query(SELECT + " WHERE disponible = 1 ORDER BY producto_id, precio, id", MAPPER);
	}

	public boolean tieneVariantesDisponibles(Integer productoId) {
		Integer cantidad = jdbc.queryForObject(
				"SELECT COUNT(*) FROM variante_producto WHERE producto_id = ? AND disponible = 1",
				Integer.class, productoId);
		return cantidad != null && cantidad > 0;
	}

	public Optional<VarianteProducto> buscarPorId(Integer id) {
		return jdbc.query(SELECT + " WHERE id = ?", MAPPER, id).stream().findFirst();
	}

	public VarianteProducto insertar(VarianteProducto variante) {
		boolean disponible = DaoUtil.verdaderoPorDefecto(variante.getDisponible());
		int id = DaoUtil.insertarConId(jdbc,
				"INSERT INTO variante_producto (producto_id, nombre, precio, disponible) VALUES (?, ?, ?, ?)",
				ps -> {
					ps.setInt(1, variante.getProductoId());
					ps.setString(2, variante.getNombre());
					ps.setBigDecimal(3, variante.getPrecio());
					ps.setBoolean(4, disponible);
				});
		variante.setId(id);
		variante.setDisponible(disponible);
		return variante;
	}

	public boolean actualizar(VarianteProducto variante) {
		boolean disponible = DaoUtil.verdaderoPorDefecto(variante.getDisponible());
		return jdbc.update(
				"UPDATE variante_producto SET nombre = ?, precio = ?, disponible = ? WHERE id = ?",
				variante.getNombre(), variante.getPrecio(), disponible, variante.getId()) > 0;
	}

	public boolean eliminar(Integer id) {
		return jdbc.update("DELETE FROM variante_producto WHERE id = ?", id) > 0;
	}
}
