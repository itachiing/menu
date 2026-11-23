package com.restaurante.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;

import com.restaurante.menu.config.GeminiProperties;
import com.restaurante.menu.dto.CategoriaMenu;
import com.restaurante.menu.entity.Adicional;
import com.restaurante.menu.entity.Categoria;
import com.restaurante.menu.entity.Producto;
import com.restaurante.menu.entity.VarianteProducto;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

	@Mock
	private GeminiClient geminiClient;

	@Mock
	private MenuService menuService;

	@Mock
	private AdicionalService adicionalService;

	private GeminiProperties geminiProperties;
	private ChatbotService chatbotService;
	private MockHttpSession session;

	@BeforeEach
	void setUp() {
		geminiProperties = new GeminiProperties(
				"test-key",
				"gemini-flash-latest",
				"https://generativelanguage.googleapis.com/v1beta",
				0.4,
				500,
				Duration.ofSeconds(5),
				Duration.ofSeconds(20),
				4 // Máximo historial reducido para probar recorte
		);

		chatbotService = new ChatbotService(
				geminiClient,
				geminiProperties,
				menuService,
				adicionalService,
				"Asados al Carbón",
				"Sabor que enciende",
				"573001234567"
		);

		session = new MockHttpSession();
	}

	@Test
	void instruccionSistemaIncluyeProductosVariantesYAdicionalesConPrecios() {
		Categoria cat = new Categoria(1, "Hamburguesas", 1, true);
		Producto prod = new Producto(10, 1, "Burguer Especial", "Carne 150g", new BigDecimal("18000"), null, true);
		VarianteProducto var = new VarianteProducto(100, 10, "Doble carne", new BigDecimal("24000"), true);
		CategoriaMenu.ProductoMenu pm = new CategoriaMenu.ProductoMenu(prod, List.of(var));
		CategoriaMenu cm = new CategoriaMenu(cat, List.of(pm));

		when(menuService.obtenerMenu()).thenReturn(List.of(cm));

		Adicional ad = new Adicional(5, null, "Papas rústicas", new BigDecimal("4500"), true);
		when(adicionalService.listar(null, true)).thenReturn(List.of(ad));

		String instruccion = chatbotService.obtenerInstruccionSistema();

		assertNotNull(instruccion);
		assertTrue(instruccion.contains("Asados al Carbón"));
		assertTrue(instruccion.contains("Burguer Especial"));
		assertTrue(instruccion.contains("$18.000"));
		assertTrue(instruccion.contains("Doble carne"));
		assertTrue(instruccion.contains("$24.000"));
		assertTrue(instruccion.contains("Papas rústicas"));
		assertTrue(instruccion.contains("$4.500"));
	}

	@Test
	void procesarMensajeExitosoActualizaHistorialEnSesion() {
		when(geminiClient.generarRespuesta(anyString(), any())).thenReturn("Te recomiendo la Burguer Especial.");

		String respuesta = chatbotService.procesarMensaje("¿Qué me recomiendas?", session, "192.168.1.10");

		assertEquals("Te recomiendo la Burguer Especial.", respuesta);

		Object attr = session.getAttribute("CHATBOT_HISTORIAL");
		assertNotNull(attr);
		@SuppressWarnings("unchecked")
		List<GeminiClient.MensajeHistorial> historial = (List<GeminiClient.MensajeHistorial>) attr;
		assertEquals(2, historial.size());
		assertEquals("user", historial.get(0).role());
		assertEquals("¿Qué me recomiendas?", historial.get(0).texto());
		assertEquals("model", historial.get(1).role());
		assertEquals("Te recomiendo la Burguer Especial.", historial.get(1).texto());
	}

	@Test
	void procesarMensajeRecortaHistorialAlSuperarMaximo() {
		when(geminiClient.generarRespuesta(anyString(), any())).thenReturn("Respuesta del bot");

		// Enviamos 3 mensajes (cada uno agrega 2 entradas: user + model = 6 entradas, maxHistorial = 4)
		chatbotService.procesarMensaje("Mensaje 1", session, "192.168.1.10");
		chatbotService.procesarMensaje("Mensaje 2", session, "192.168.1.10");
		chatbotService.procesarMensaje("Mensaje 3", session, "192.168.1.10");

		@SuppressWarnings("unchecked")
		List<GeminiClient.MensajeHistorial> historial = (List<GeminiClient.MensajeHistorial>) session.getAttribute("CHATBOT_HISTORIAL");
		assertEquals(4, historial.size());
	}

	@Test
	void procesarMensajeRechazaMensajeVacio() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
				chatbotService.procesarMensaje("   ", session, "192.168.1.10")
		);
		assertTrue(ex.getMessage().contains("obligatorio"));
	}

	@Test
	void procesarMensajeRechazaMensajeMuyLargo() {
		String textoLargo = "A".repeat(501);
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
				chatbotService.procesarMensaje(textoLargo, session, "192.168.1.10")
		);
		assertTrue(ex.getMessage().contains("500"));
	}

	@Test
	void rateLimitLanzaExcepcion429AlSuperar15MensajesPorMinuto() {
		when(geminiClient.generarRespuesta(anyString(), any())).thenReturn("OK");

		// Enviamos 15 mensajes permitidos
		for (int i = 0; i < 15; i++) {
			chatbotService.procesarMensaje("Mensaje " + i, session, "10.0.0.1");
		}

		// El mensaje número 16 debe ser rechazado con 429
		ChatbotException ex = assertThrows(ChatbotException.class, () ->
				chatbotService.procesarMensaje("Mensaje 16", session, "10.0.0.1")
		);
		assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getEstadoHttp());
		assertTrue(ex.getMessage().contains("muy rápido"));
	}

	@Test
	void limpiarHistorialEliminaAtributoDeSesion() {
		session.setAttribute("CHATBOT_HISTORIAL", List.of(new GeminiClient.MensajeHistorial("user", "hola")));
		chatbotService.limpiarHistorial(session);
		assertNull(session.getAttribute("CHATBOT_HISTORIAL"));
	}
}
