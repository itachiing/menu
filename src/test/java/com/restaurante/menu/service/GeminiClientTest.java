package com.restaurante.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.restaurante.menu.config.GeminiProperties;

class GeminiClientTest {

	private MockRestServiceServer mockServer;
	private GeminiClient geminiClient;
	private GeminiProperties properties;

	@BeforeEach
	void setUp() {
		properties = new GeminiProperties(
				"clave-de-prueba",
				"gemini-flash-latest",
				"https://generativelanguage.googleapis.com/v1beta",
				0.4,
				500,
				Duration.ofSeconds(5),
				Duration.ofSeconds(20),
				10
		);

		RestClient.Builder builder = RestClient.builder();
		mockServer = MockRestServiceServer.bindTo(builder).build();
		RestClient restClient = builder.baseUrl(properties.baseUrl()).build();

		geminiClient = new GeminiClient(restClient, properties);
	}

	@Test
	void generarRespuestaExitosoDevuelveTextoDelModelo() {
		String jsonRespuesta = """
				{
				  "candidates": [
				    {
				      "content": {
				        "parts": [
				          { "text": "Nuestras hamburguesas son al carbón y vienen con papas." }
				        ],
				        "role": "model"
				      },
				      "finishReason": "STOP"
				    }
				  ]
				}
				""";

		mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("x-goog-api-key", "clave-de-prueba"))
				.andRespond(withSuccess(jsonRespuesta, MediaType.APPLICATION_JSON));

		List<GeminiClient.MensajeHistorial> historial = List.of(
				new GeminiClient.MensajeHistorial("user", "¿Qué tienen de comer?")
		);

		String respuesta = geminiClient.generarRespuesta("Instrucción del sistema", historial);

		assertEquals("Nuestras hamburguesas son al carbón y vienen con papas.", respuesta);
		mockServer.verify();
	}

	@Test
	void generarRespuestaSinApiKeyConfiguradaLanzaExcepcion503() {
		GeminiProperties propsSinKey = new GeminiProperties(
				"",
				"gemini-flash-latest",
				"https://generativelanguage.googleapis.com/v1beta",
				0.4,
				500,
				Duration.ofSeconds(5),
				Duration.ofSeconds(20),
				10
		);
		RestClient restClient = RestClient.builder().baseUrl(propsSinKey.baseUrl()).build();
		GeminiClient clienteSinKey = new GeminiClient(restClient, propsSinKey);

		ChatbotException ex = assertThrows(ChatbotException.class, () ->
				clienteSinKey.generarRespuesta("test", List.of(new GeminiClient.MensajeHistorial("user", "hola")))
		);

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getEstadoHttp());
		assertTrue(ex.getMessage().contains("no está disponible"));
	}

	@Test
	void generarRespuestaCuotaExcedida429LanzaExcepcion429() {
		mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

		ChatbotException ex = assertThrows(ChatbotException.class, () ->
				geminiClient.generarRespuesta("test", List.of(new GeminiClient.MensajeHistorial("user", "hola")))
		);

		assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getEstadoHttp());
		assertTrue(ex.getMessage().contains("muchas consultas"));
		mockServer.verify();
	}

	@Test
	void generarRespuestaClaveInvalida403LanzaExcepcion503() {
		mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.FORBIDDEN));

		ChatbotException ex = assertThrows(ChatbotException.class, () ->
				geminiClient.generarRespuesta("test", List.of(new GeminiClient.MensajeHistorial("user", "hola")))
		);

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getEstadoHttp());
		mockServer.verify();
	}

	@Test
	void generarRespuestaBloqueadaPorSeguridadDevuelveMensajeNeutro() {
		String jsonBloqueado = """
				{
				  "promptFeedback": {
				    "blockReason": "SAFETY"
				  },
				  "candidates": []
				}
				""";

		mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess(jsonBloqueado, MediaType.APPLICATION_JSON));

		String respuesta = geminiClient.generarRespuesta("test", List.of(new GeminiClient.MensajeHistorial("user", "pregunta")));

		assertTrue(respuesta.contains("No pude procesar esa consulta"));
		mockServer.verify();
	}

	@Test
	void generarRespuestaCandidatosVaciosDevuelveMensajeNeutro() {
		String jsonVacio = """
				{
				  "candidates": []
				}
				""";

		mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess(jsonVacio, MediaType.APPLICATION_JSON));

		String respuesta = geminiClient.generarRespuesta("test", List.of(new GeminiClient.MensajeHistorial("user", "pregunta")));

		assertTrue(respuesta.contains("No pude procesar esa consulta"));
		mockServer.verify();
	}
}
