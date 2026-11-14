package com.restaurante.menu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.restaurante.menu.entity.Usuario;

/**
 * Acceso a los usuarios del panel administrativo.
 */
@Repository
public class UsuarioRepository {

	private static final String SELECT = "SELECT id, username, password, nombre, rol, activo FROM usuario";

	private static final RowMapper<Usuario> MAPPER = (rs, fila) -> new Usuario(
			rs.getInt("id"),
			rs.getString("username"),
			rs.getString("password"),
			rs.getString("nombre"),
			rs.getString("rol"),
			rs.getBoolean("activo"));

	private final JdbcTemplate jdbc;

	public UsuarioRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public List<Usuario> listar() {
		return jdbc.query(SELECT + " ORDER BY username", MAPPER);
	}

	public Optional<Usuario> buscarPorId(Integer id) {
		return jdbc.query(SELECT + " WHERE id = ?", MAPPER, id).stream().findFirst();
	}

	public Optional<Usuario> buscarPorUsername(String username) {
		return jdbc.query(SELECT + " WHERE username = ?", MAPPER, username).stream().findFirst();
	}

	public Usuario insertar(Usuario usuario) {
		boolean activo = RepositoryUtil.verdaderoPorDefecto(usuario.getActivo());
		int id = RepositoryUtil.insertarConId(jdbc,
				"INSERT INTO usuario (username, password, nombre, rol, activo) VALUES (?, ?, ?, ?, ?)",
				ps -> {
					ps.setString(1, usuario.getUsername());
					ps.setString(2, usuario.getPassword());
					ps.setString(3, usuario.getNombre());
					ps.setString(4, usuario.getRol());
					ps.setBoolean(5, activo);
				});
		usuario.setId(id);
		usuario.setActivo(activo);
		return usuario;
	}

	/**
	 * Actualiza los datos del usuario sin tocar la contraseña.
	 */
	public boolean actualizar(Usuario usuario) {
		boolean activo = RepositoryUtil.verdaderoPorDefecto(usuario.getActivo());
		return jdbc.update(
				"UPDATE usuario SET username = ?, nombre = ?, rol = ?, activo = ? WHERE id = ?",
				usuario.getUsername(), usuario.getNombre(), usuario.getRol(), activo,
				usuario.getId()) > 0;
	}

	public boolean cambiarPassword(Integer id, String passwordCifrada) {
		return jdbc.update("UPDATE usuario SET password = ? WHERE id = ?", passwordCifrada, id) > 0;
	}

	public boolean eliminar(Integer id) {
		return jdbc.update("DELETE FROM usuario WHERE id = ?", id) > 0;
	}
}
