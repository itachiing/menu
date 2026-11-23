package com.restaurante.menu.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración para la integración con la API de Google Gemini.
 */
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
		String apiKey,
		String model,
		String baseUrl,
		Double temperature,
		Integer maxOutputTokens,
		Duration connectTimeout,
		Duration readTimeout,
		Integer maxHistorial
) {
	public GeminiProperties {
		if (model == null || model.isBlank()) {
			model = "gemini-flash-latest";
		}
		if (baseUrl == null || baseUrl.isBlank()) {
			baseUrl = "https://generativelanguage.googleapis.com/v1beta";
		}
		if (temperature == null) {
			temperature = 0.4;
		}
		if (maxOutputTokens == null) {
			maxOutputTokens = 500;
		}
		if (connectTimeout == null) {
			connectTimeout = Duration.ofSeconds(5);
		}
		if (readTimeout == null) {
			readTimeout = Duration.ofSeconds(20);
		}
		if (maxHistorial == null) {
			maxHistorial = 10;
		}
	}

	public boolean tieneApiKey() {
		return apiKey != null && !apiKey.isBlank();
	}
}
