package com.restaurante.menu.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.menu.dto.ChatbotRequest;
import com.restaurante.menu.dto.ChatbotRespuesta;
import com.restaurante.menu.service.ChatbotService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Endpoints REST del chatbot de asistencia al cliente en el menú digital.
 */
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

	private final ChatbotService chatbotService;

	public ChatbotController(ChatbotService chatbotService) {
		this.chatbotService = chatbotService;
	}

	@PostMapping("/mensaje")
	public ChatbotRespuesta enviarMensaje(@RequestBody ChatbotRequest request, HttpServletRequest httpRequest) {
		String mensaje = (request != null) ? request.mensaje() : null;
		HttpSession session = httpRequest.getSession(true);
		String ip = obtenerIpCliente(httpRequest);

		String respuesta = chatbotService.procesarMensaje(mensaje, session, ip);
		return new ChatbotRespuesta(respuesta);
	}

	@DeleteMapping("/historial")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reiniciarHistorial(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession(false);
		if (session != null) {
			chatbotService.limpiarHistorial(session);
		}
	}

	private String obtenerIpCliente(HttpServletRequest request) {
		String headerXForwardedFor = request.getHeader("X-Forwarded-For");
		if (headerXForwardedFor != null && !headerXForwardedFor.isBlank()) {
			return headerXForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
