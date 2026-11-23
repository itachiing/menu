package com.restaurante.menu.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.menu.dto.ChatbotRequest;
import com.restaurante.menu.service.ChatbotException;
import com.restaurante.menu.service.ChatbotService;

@ExtendWith(MockitoExtension.class)
class ChatbotControllerTest {

	@Mock
	private ChatbotService chatbotService;

	@InjectMocks
	private ChatbotController chatbotController;

	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		mockMvc = standaloneSetup(chatbotController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void enviarMensajeValidoDevuelve200ConRespuesta() throws Exception {
		ChatbotRequest request = new ChatbotRequest("¿Qué hamburguesas tienen?");
		when(chatbotService.procesarMensaje(eq("¿Qué hamburguesas tienen?"), any(), any()))
				.thenReturn("Tenemos Burguer al Carbón y Burguer Doble.");

		mockMvc.perform(post("/api/chatbot/mensaje")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.respuesta").value("Tenemos Burguer al Carbón y Burguer Doble."));
	}

	@Test
	void enviarMensajeVacioDevuelve400BadRequest() throws Exception {
		ChatbotRequest request = new ChatbotRequest("   ");
		when(chatbotService.procesarMensaje(eq("   "), any(), any()))
				.thenThrow(new IllegalArgumentException("El campo 'mensaje' es obligatorio"));

		mockMvc.perform(post("/api/chatbot/mensaje")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("El campo 'mensaje' es obligatorio"));
	}

	@Test
	void enviarMensajeConCuotaExcedidaDevuelve429() throws Exception {
		ChatbotRequest request = new ChatbotRequest("hola");
		when(chatbotService.procesarMensaje(eq("hola"), any(), any()))
				.thenThrow(new ChatbotException(HttpStatus.TOO_MANY_REQUESTS, "Muchas consultas"));

		mockMvc.perform(post("/api/chatbot/mensaje")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.error").value("Muchas consultas"));
	}

	@Test
	void enviarMensajeSinApiKeyDevuelve503() throws Exception {
		ChatbotRequest request = new ChatbotRequest("hola");
		when(chatbotService.procesarMensaje(eq("hola"), any(), any()))
				.thenThrow(new ChatbotException(HttpStatus.SERVICE_UNAVAILABLE, "El asistente no está disponible por ahora"));

		mockMvc.perform(post("/api/chatbot/mensaje")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.error").value("El asistente no está disponible por ahora"));
	}

	@Test
	void reiniciarHistorialDevuelve204NoContent() throws Exception {
		org.springframework.mock.web.MockHttpSession session = new org.springframework.mock.web.MockHttpSession();

		mockMvc.perform(delete("/api/chatbot/historial").session(session))
				.andExpect(status().isNoContent());

		org.mockito.Mockito.verify(chatbotService).limpiarHistorial(eq(session));
	}
}
