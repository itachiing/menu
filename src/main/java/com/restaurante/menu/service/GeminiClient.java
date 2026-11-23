package com.restaurante.menu.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.restaurante.menu.config.GeminiProperties;

/**
 * Cliente HTTP para comunicarse con la API de Google Gemini.
 * Se encarga exclusivamente de armar la petición, llamar al endpoint generateContent,
 * parsear la respuesta y traducir posibles fallos a {@link ChatbotException}.
 */
@Service
public class GeminiClient {

	private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
	private static final String MENSAJE_FALLBACK = "No pude procesar esa consulta. Recuerda que puedo orientarte sobre nuestra carta, platos y precios.";

	private final RestClient restClient;
	private final GeminiProperties properties;

	public GeminiClient(RestClient geminiRestClient, GeminiProperties properties) {
		this.restClient = geminiRestClient;
		this.properties = properties;
	}

	/**
	 * Envía el contexto y la conversación a Gemini para generar una respuesta de texto.
	 *
	 * @param instruccionSistema Reglas y menú del restaurante que condicionan el comportamiento del bot.
	 * @param historial Conversación previa y mensaje actual formateados como roles 'user' y 'model'.
	 * @return Texto de respuesta generado por el modelo.
	 */
	public String generarRespuesta(String instruccionSistema, List<MensajeHistorial> historial) {
		if (!properties.tieneApiKey()) {
			log.warn("Llamada al chatbot sin GEMINI_API_KEY configurada.");
			throw new ChatbotException(HttpStatus.SERVICE_UNAVAILABLE,
					"El asistente no está disponible por ahora, puedes pedir por WhatsApp");
		}

		GeminiRequest requestBody = construirRequest(instruccionSistema, historial);
		String path = "/models/" + properties.model() + ":generateContent";

		try {
			GeminiResponse response = restClient.post()
					.uri(path)
					.header("x-goog-api-key", properties.apiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestBody)
					.retrieve()
					.body(GeminiResponse.class);

			return procesarRespuesta(response);

		} catch (RestClientResponseException e) {
			int statusCode = e.getStatusCode().value();
			log.warn("Gemini respondió con código de error HTTP {}: {}", statusCode, e.getResponseBodyAsString());

			if (statusCode == 429) {
				throw new ChatbotException(HttpStatus.TOO_MANY_REQUESTS,
						"El asistente ha recibido muchas consultas en este momento. Por favor, intenta de nuevo en unos momentos.");
			} else if (statusCode == 400 || statusCode == 401 || statusCode == 403) {
				throw new ChatbotException(HttpStatus.SERVICE_UNAVAILABLE,
						"El asistente no está disponible por ahora, puedes pedir por WhatsApp");
			} else {
				throw new ChatbotException(HttpStatus.SERVICE_UNAVAILABLE,
						"El asistente no está disponible por ahora, puedes pedir por WhatsApp");
			}
		} catch (ResourceAccessException e) {
			log.warn("Timeout o error de conexión con la API de Gemini: {}", e.getMessage());
			throw new ChatbotException(HttpStatus.GATEWAY_TIMEOUT,
					"No fue posible conectar con el asistente. Puedes comunicarte por WhatsApp.");
		} catch (ChatbotException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error inesperado al invocar Gemini: {}", e.getMessage());
			throw new ChatbotException(HttpStatus.SERVICE_UNAVAILABLE,
					"El asistente no está disponible por ahora, puedes pedir por WhatsApp");
		}
	}

	private GeminiRequest construirRequest(String instruccionSistema, List<MensajeHistorial> historial) {
		InstruccionSistema systemInstruction = null;
		if (instruccionSistema != null && !instruccionSistema.isBlank()) {
			systemInstruction = new InstruccionSistema(List.of(new ParteTexto(instruccionSistema)));
		}

		List<ContenidoMensaje> contents = historial.stream()
				.map(m -> new ContenidoMensaje(m.role(), List.of(new ParteTexto(m.texto()))))
				.toList();

		ConfiguracionGeneracion config = new ConfiguracionGeneracion(
				properties.temperature(),
				properties.maxOutputTokens()
		);

		return new GeminiRequest(systemInstruction, contents, config);
	}

	private String procesarRespuesta(GeminiResponse response) {
		if (response == null) {
			return MENSAJE_FALLBACK;
		}

		if (response.promptFeedback() != null && response.promptFeedback().blockReason() != null) {
			log.warn("Prompt bloqueado por Gemini. Razón: {}", response.promptFeedback().blockReason());
			return MENSAJE_FALLBACK;
		}

		if (response.candidates() == null || response.candidates().isEmpty()) {
			return MENSAJE_FALLBACK;
		}

		Candidato primerCandidato = response.candidates().get(0);
		if (primerCandidato.content() == null || primerCandidato.content().parts() == null
				|| primerCandidato.content().parts().isEmpty()) {
			log.warn("Candidato sin contenido. FinishReason: {}", primerCandidato.finishReason());
			return MENSAJE_FALLBACK;
		}

		String texto = primerCandidato.content().parts().get(0).text();
		return (texto != null && !texto.isBlank()) ? texto.trim() : MENSAJE_FALLBACK;
	}

	// --- DTOs internos para el protocolo de Gemini API ---

	public record MensajeHistorial(String role, String texto) {
	}

	public record GeminiRequest(
			@JsonProperty("system_instruction") InstruccionSistema systemInstruction,
			@JsonProperty("contents") List<ContenidoMensaje> contents,
			@JsonProperty("generationConfig") ConfiguracionGeneracion generationConfig
	) {
	}

	public record InstruccionSistema(
			@JsonProperty("parts") List<ParteTexto> parts
	) {
	}

	public record ContenidoMensaje(
			@JsonProperty("role") String role,
			@JsonProperty("parts") List<ParteTexto> parts
	) {
	}

	public record ParteTexto(
			@JsonProperty("text") String text
	) {
	}

	public record ConfiguracionGeneracion(
			@JsonProperty("temperature") Double temperature,
			@JsonProperty("maxOutputTokens") Integer maxOutputTokens
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record GeminiResponse(
			@JsonProperty("candidates") List<Candidato> candidates,
			@JsonProperty("promptFeedback") PromptFeedback promptFeedback
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Candidato(
			@JsonProperty("content") ContenidoMensaje content,
			@JsonProperty("finishReason") String finishReason
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record PromptFeedback(
			@JsonProperty("blockReason") String blockReason
	) {
	}
}
