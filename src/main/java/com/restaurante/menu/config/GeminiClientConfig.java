package com.restaurante.menu.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración del cliente HTTP RestClient para la comunicación con Google Gemini.
 */
@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiClientConfig {

	@Bean
	public RestClient geminiRestClient(GeminiProperties properties, RestClient.Builder builder) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(properties.connectTimeout());
		requestFactory.setReadTimeout(properties.readTimeout());

		return builder
				.baseUrl(properties.baseUrl())
				.requestFactory(requestFactory)
				.build();
	}
}
