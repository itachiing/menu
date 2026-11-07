package com.restaurante.menu.model.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.restaurante.menu.vo.Categoria;

@Repository
public class CategoriaDAO {

	private static final String SELECT = "SELECT id, nombre, orden, activa FROM categoria";

	private static final RowMapper<Categoria> MAPPER = (rs, fila) -> new Categoria(
			rs.getInt("id"),
			rs.getString("nombre"),
			rs.getInt("orden"),
			rs.getBoolean("activa"));

	private final JdbcTemplate jdbc;

	public CategoriaDAO(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public List<Categoria> listar(boolean soloActivas) {
		String sql = SELECT + (soloActivas ? " WHERE activa = 1" : "") + " ORDER BY orden, nombre";
		return jdbc.query(sql, MAPPER);
	}

	public Optional<Categoria> buscarPorId(Integer id) {
		return jdbc.query(SELECT + " WHERE id = ?", MAPPER, id).stream().findFirst();
	}

	public Categoria insertar(Categoria categoria) {
		int orden = categoria.getOrden() == null ? 0 : categoria.getOrden();
		boolean activa = DaoUtil.verdaderoPorDefecto(categoria.getActiva());
		int id = DaoUtil.insertarConId(jdbc,
				"INSERT INTO categoria (nombre, orden, activa) VALUES (?, ?, ?)",
				ps -> {
					ps.setString(1, categoria.getNombre());
					ps.setInt(2, orden);
					ps.setBoolean(3, activa);
				});
		categoria.setId(id);
		categoria.setOrden(orden);
		categoria.setActiva(activa);
		return categoria;
	}

	public boolean actualizar(Categoria categoria) {
		int orden = categoria.getOrden() == null ? 0 : categoria.getOrden();
		boolean activa = DaoUtil.verdaderoPorDefecto(categoria.getActiva());
		return jdbc.update(
				"UPDATE categoria SET nombre = ?, orden = ?, activa = ? WHERE id = ?",
				categoria.getNombre(), orden, activa, categoria.getId()) > 0;
	}

	public boolean eliminar(Integer id) {
		return jdbc.update("DELETE FROM categoria WHERE id = ?", id) > 0;
	}
}
