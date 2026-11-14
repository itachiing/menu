package com.restaurante.menu.service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Validaciones sencillas de los datos que llegan a los controllers.
 * Las excepciones se convierten en respuestas HTTP en {@link com.restaurante.menu.controller.GlobalExceptionHandler}.
 */
public final class Validaciones {

	private Validaciones() {
	}

	/** Texto obligatorio, sin espacios sobrantes y con longitud máxima. */
	public static String texto(String valor, String campo, int maximo) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio");
		}
		return limitar(valor.trim(), campo, maximo);
	}

	/** Texto opcional: devuelve null si viene vacío. */
	public static String opcional(String valor, String campo, int maximo) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		return limitar(valor.trim(), campo, maximo);
	}

	/** Valor monetario obligatorio y no negativo. */
	public static BigDecimal precio(BigDecimal valor, String campo) {
		if (valor == null || valor.signum() < 0) {
			throw new IllegalArgumentException(
					"El campo '" + campo + "' es obligatorio y no puede ser negativo");
		}
		return valor;
	}

	/** Devuelve el valor o lanza NoSuchElementException (404) con el nombre de lo que no se halló. */
	public static <T> T encontrado(Optional<T> valor, String descripcion) {
		return valor.orElseThrow(() -> new NoSuchElementException(descripcion + " no encontrado"));
	}

	private static String limitar(String texto, String campo, int maximo) {
		if (texto.length() > maximo) {
			throw new IllegalArgumentException(
					"El campo '" + campo + "' no puede tener más de " + maximo + " caracteres");
		}
		return texto;
	}
}
