package com.restaurante.menu.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Utilidades compartidas por los DAO.
 */
final class DaoUtil {

	private DaoUtil() {
	}

	/**
	 * Ejecuta un INSERT y devuelve el id autogenerado.
	 */
	static int insertarConId(JdbcTemplate jdbc, String sql, PreparedStatementSetter valores) {
		KeyHolder keys = new GeneratedKeyHolder();
		jdbc.update(con -> {
			PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			valores.setValues(ps);
			return ps;
		}, keys);
		Number id = keys.getKey();
		if (id == null) {
			throw new IllegalStateException("La base de datos no devolvió el id generado");
		}
		return id.intValue();
	}

	/**
	 * Lee una columna entera que puede ser NULL.
	 */
	static Integer enteroONulo(ResultSet rs, String columna) throws SQLException {
		int valor = rs.getInt(columna);
		return rs.wasNull() ? null : valor;
	}

	/**
	 * Asigna un parámetro entero que puede ser NULL.
	 */
	static void asignarEntero(PreparedStatement ps, int indice, Integer valor) throws SQLException {
		if (valor == null) {
			ps.setNull(indice, Types.INTEGER);
		} else {
			ps.setInt(indice, valor);
		}
	}

	/**
	 * Los campos booleanos opcionales (activa, disponible, activo) valen true si no se envían.
	 */
	static boolean verdaderoPorDefecto(Boolean valor) {
		return valor == null || valor;
	}
}
