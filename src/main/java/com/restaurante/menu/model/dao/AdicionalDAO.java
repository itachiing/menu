package com.restaurante.menu.model.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.restaurante.menu.vo.Adicional;

@Repository
public class AdicionalDAO {

	private static final String SELECT =
			"SELECT id, categoria_id, nombre, precio, disponible FROM adicional";

	private static final RowMapper<Adicional> MAPPER = (rs, fila) -> new Adicional(
			rs.getInt("id"),
			DaoUtil.enteroONulo(rs, "categoria_id"),
			rs.getString("nombre"),
			rs.getBigDecimal("precio"),
			rs.getBoolean("disponible"));

	private final JdbcTemplate jdbc;

	public AdicionalDAO(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	/**
	 * Si se indica categoriaId devuelve los adicionales de esa categoría más los generales
	 * (categoria_id NULL). Sin categoriaId devuelve todos.
	 */
	public List<Adicional> listar(Integer categoriaId, boolean soloDisponibles) {
		StringBuilder sql = new StringBuilder(SELECT);
		List<String> filtros = new ArrayList<>();
		List<Object> args = new ArrayList<>();
		if (categoriaId != null) {
			filtros.add("(categoria_id = ? OR categoria_id IS NULL)");
			args.add(categoriaId);
		}
		if (soloDisponibles) {
			filtros.add("disponible = 1");
		}
		if (!filtros.isEmpty()) {
			sql.append(" WHERE ").append(String.join(" AND ", filtros));
		}
		sql.append(" ORDER BY nombre");
		return jdbc.query(sql.toString(), MAPPER, args.toArray());
	}

	public Optional<Adicional> buscarPorId(Integer id) {
		return jdbc.query(SELECT + " WHERE id = ?", MAPPER, id).stream().findFirst();
	}

	public Adicional insertar(Adicional adicional) {
		boolean disponible = DaoUtil.verdaderoPorDefecto(adicional.getDisponible());
		BigDecimal precio = adicional.getPrecio() == null ? BigDecimal.ZERO : adicional.getPrecio();
		int id = DaoUtil.insertarConId(jdbc,
				"INSERT INTO adicional (categoria_id, nombre, precio, disponible) VALUES (?, ?, ?, ?)",
				ps -> {
					DaoUtil.asignarEntero(ps, 1, adicional.getCategoriaId());
					ps.setString(2, adicional.getNombre());
					ps.setBigDecimal(3, precio);
					ps.setBoolean(4, disponible);
				});
		adicional.setId(id);
		adicional.setPrecio(precio);
		adicional.setDisponible(disponible);
		return adicional;
	}

	public boolean actualizar(Adicional adicional) {
		boolean disponible = DaoUtil.verdaderoPorDefecto(adicional.getDisponible());
		BigDecimal precio = adicional.getPrecio() == null ? BigDecimal.ZERO : adicional.getPrecio();
		return jdbc.update(
				"UPDATE adicional SET categoria_id = ?, nombre = ?, precio = ?, disponible = ? WHERE id = ?",
				adicional.getCategoriaId(), adicional.getNombre(), precio, disponible,
				adicional.getId()) > 0;
	}

	public boolean eliminar(Integer id) {
		return jdbc.update("DELETE FROM adicional WHERE id = ?", id) > 0;
	}
}
