package com.restaurante.menu.model.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.restaurante.menu.vo.Producto;

@Repository
public class ProductoDAO {

	private static final String SELECT =
			"SELECT id, categoria_id, nombre, descripcion, precio, imagen, disponible FROM producto";

	private static final RowMapper<Producto> MAPPER = (rs, fila) -> new Producto(
			rs.getInt("id"),
			rs.getInt("categoria_id"),
			rs.getString("nombre"),
			rs.getString("descripcion"),
			rs.getBigDecimal("precio"),
			rs.getString("imagen"),
			rs.getBoolean("disponible"));

	private final JdbcTemplate jdbc;

	public ProductoDAO(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	/**
	 * Lista productos; categoriaId puede ser null para traerlos todos.
	 */
	public List<Producto> listar(Integer categoriaId, boolean soloDisponibles) {
		StringBuilder sql = new StringBuilder(SELECT);
		List<String> filtros = new ArrayList<>();
		List<Object> args = new ArrayList<>();
		if (categoriaId != null) {
			filtros.add("categoria_id = ?");
			args.add(categoriaId);
		}
		if (soloDisponibles) {
			filtros.add("disponible = 1");
		}
		if (!filtros.isEmpty()) {
			sql.append(" WHERE ").append(String.join(" AND ", filtros));
		}
		sql.append(" ORDER BY categoria_id, id");
		return jdbc.query(sql.toString(), MAPPER, args.toArray());
	}

	public Optional<Producto> buscarPorId(Integer id) {
		return jdbc.query(SELECT + " WHERE id = ?", MAPPER, id).stream().findFirst();
	}

	public Producto insertar(Producto producto) {
		boolean disponible = DaoUtil.verdaderoPorDefecto(producto.getDisponible());
		int id = DaoUtil.insertarConId(jdbc,
				"INSERT INTO producto (categoria_id, nombre, descripcion, precio, imagen, disponible) "
						+ "VALUES (?, ?, ?, ?, ?, ?)",
				ps -> {
					ps.setInt(1, producto.getCategoriaId());
					ps.setString(2, producto.getNombre());
					ps.setString(3, producto.getDescripcion());
					ps.setBigDecimal(4, producto.getPrecio());
					ps.setString(5, producto.getImagen());
					ps.setBoolean(6, disponible);
				});
		producto.setId(id);
		producto.setDisponible(disponible);
		return producto;
	}

	public boolean actualizar(Producto producto) {
		boolean disponible = DaoUtil.verdaderoPorDefecto(producto.getDisponible());
		return jdbc.update(
				"UPDATE producto SET categoria_id = ?, nombre = ?, descripcion = ?, precio = ?, "
						+ "imagen = ?, disponible = ? WHERE id = ?",
				producto.getCategoriaId(), producto.getNombre(), producto.getDescripcion(),
				producto.getPrecio(), producto.getImagen(), disponible, producto.getId()) > 0;
	}

	public boolean cambiarDisponibilidad(Integer id, boolean disponible) {
		return jdbc.update("UPDATE producto SET disponible = ? WHERE id = ?", disponible, id) > 0;
	}

	public boolean eliminar(Integer id) {
		return jdbc.update("DELETE FROM producto WHERE id = ?", id) > 0;
	}
}
