package com.restaurante.menu.service;

import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para incidencias del chatbot con su respectivo código HTTP.
 */
public class ChatbotException extends RuntimeException {

	private final HttpStatus estadoHttp;

	public ChatbotException(HttpStatus estadoHttp, String mensaje) {
		super(mensaje);
		this.estadoHttp = estadoHttp;
	}

	public HttpStatus getEstadoHttp() {
		return estadoHttp;
	}
}
