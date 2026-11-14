package com.restaurante.menu.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.restaurante.menu.entity.Mesa;

@Repository
public class MesaRepository {

	private static final String SELECT = "SELECT id, numero, codigo, activa FROM mesa";

	private static final RowMapper<Mesa> MAPPER = (rs, fila) -> new Mesa(
			rs.getInt("id"),
			rs.getInt("numero"),
			rs.getString("codigo"),
			rs.getBoolean("activa"));

	private final JdbcTemplate jdbc;

	public MesaRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public List<Mesa> listar() {
		return jdbc.query(SELECT + " ORDER BY numero", MAPPER);
	}

	public Optional<Mesa> buscarPorId(Integer id) {
		return jdbc.query(SELECT + " WHERE id = ?", MAPPER, id).stream().findFirst();
	}

	/**
	 * Busca la mesa por el código que va en el QR.
	 */
	public Optional<Mesa> buscarPorCodigo(String codigo) {
		return jdbc.query(SELECT + " WHERE codigo = ?", MAPPER, codigo).stream().findFirst();
	}

	/**
	 * Busca la mesa por su número visible.
	 */
	public Optional<Mesa> buscarPorNumero(Integer numero) {
		return jdbc.query(SELECT + " WHERE numero = ?", MAPPER, numero).stream().findFirst();
	}

	/**
	 * Inserta la mesa y le genera un código aleatorio para su QR.
	 */
	public Mesa insertar(Mesa mesa) {
		boolean activa = RepositoryUtil.verdaderoPorDefecto(mesa.getActiva());
		String codigo = UUID.randomUUID().toString();
		int id = RepositoryUtil.insertarConId(jdbc,
				"INSERT INTO mesa (numero, codigo, activa) VALUES (?, ?, ?)",
				ps -> {
					ps.setInt(1, mesa.getNumero());
					ps.setString(2, codigo);
					ps.setBoolean(3, activa);
				});
		mesa.setId(id);
		mesa.setCodigo(codigo);
		mesa.setActiva(activa);
		return mesa;
	}

	/**
	 * Actualiza número y estado; el código del QR no cambia (usa regenerarCodigo para eso).
	 */
	public boolean actualizar(Mesa mesa) {
		boolean activa = RepositoryUtil.verdaderoPorDefecto(mesa.getActiva());
		return jdbc.update("UPDATE mesa SET numero = ?, activa = ? WHERE id = ?",
				mesa.getNumero(), activa, mesa.getId()) > 0;
	}

	/**
	 * Genera un código nuevo; el QR impreso anterior deja de funcionar.
	 */
	public boolean regenerarCodigo(Integer id) {
		return jdbc.update("UPDATE mesa SET codigo = ? WHERE id = ?",
				UUID.randomUUID().toString(), id) > 0;
	}

	public boolean eliminar(Integer id) {
		return jdbc.update("DELETE FROM mesa WHERE id = ?", id) > 0;
	}
}
