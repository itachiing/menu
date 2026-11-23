package com.restaurante.menu.controller;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.restaurante.menu.service.ChatbotException;

/**
 * Convierte las excepciones de los DAO y controllers en respuestas JSON con el formato
 * {"error": "mensaje"}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
		return responder(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Map<String, String>> noEncontrado(NoSuchElementException e) {
		return responder(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> estadoInvalido(IllegalStateException e) {
		return responder(HttpStatus.CONFLICT, e.getMessage());
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public ResponseEntity<Map<String, String>> duplicado(DuplicateKeyException e) {
		return responder(HttpStatus.CONFLICT, "Ya existe un registro con esos datos");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException e) {
		return responder(HttpStatus.CONFLICT,
				"No se pudo completar la operación: hay datos relacionados (por ejemplo, pedidos) "
						+ "o una referencia inválida. Si quieres quitar algo del menú, márcalo como "
						+ "no disponible o inactivo en lugar de eliminarlo");
	}

	@ExceptionHandler(ChatbotException.class)
	public ResponseEntity<Map<String, String>> errorChatbot(ChatbotException e) {
		return responder(e.getEstadoHttp(), e.getMessage());
	}

	private static ResponseEntity<Map<String, String>> responder(HttpStatus estado, String mensaje) {
		return ResponseEntity.status(estado).body(Map.of("error", mensaje));
	}
}
