package com.restaurante.menu.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.restaurante.menu.entity.Categoria;
import com.restaurante.menu.service.CategoriaService;

class CategoriaControllerTest {

	private CategoriaService categoriaService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		categoriaService = mock(CategoriaService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new CategoriaController(categoriaService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void listaCategorias() throws Exception {
		when(categoriaService.listar(true)).thenReturn(List.of(new Categoria(1, "Hamburguesas", 1, true)));

		mockMvc.perform(get("/api/categorias").param("soloActivas", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Hamburguesas"));
	}

	@Test
	void creaUnaCategoria() throws Exception {
		when(categoriaService.crear(any(Categoria.class)))
				.thenReturn(new Categoria(7, "Postres", 7, true));

		mockMvc.perform(post("/api/categorias")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Postres\",\"orden\":7,\"activa\":true}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.nombre").value("Postres"));
	}
}
