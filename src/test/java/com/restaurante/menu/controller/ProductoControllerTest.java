package com.restaurante.menu.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.restaurante.menu.entity.Producto;
import com.restaurante.menu.service.ProductoService;

/**
 * Prueba los endpoints REST con MockMvc y un servicio simulado (no requiere MySQL).
 */
class ProductoControllerTest {

	private ProductoService productoService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		productoService = mock(ProductoService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new ProductoController(productoService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void listaProductos() throws Exception {
		when(productoService.listar(null, false)).thenReturn(List.of(producto(1, "Burguer al Carbón")));

		mockMvc.perform(get("/api/productos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].nombre").value("Burguer al Carbón"));
	}

	@Test
	void obtieneUnProductoPorId() throws Exception {
		when(productoService.obtener(1)).thenReturn(producto(1, "Burguer al Carbón"));

		mockMvc.perform(get("/api/productos/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Burguer al Carbón"))
				.andExpect(jsonPath("$.precio").value(12000));
	}

	@Test
	void devuelve404CuandoNoExiste() throws Exception {
		when(productoService.obtener(99)).thenThrow(new NoSuchElementException("Producto no encontrado"));

		mockMvc.perform(get("/api/productos/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Producto no encontrado"));
	}

	@Test
	void creaUnProductoYDevuelve201() throws Exception {
		when(productoService.crear(any(Producto.class))).thenReturn(producto(15, "Burguer Chorimeta"));

		mockMvc.perform(post("/api/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "categoriaId": 1,
						  "nombre": "Burguer Chorimeta",
						  "descripcion": "Con chorizo",
						  "precio": 15000,
						  "disponible": true
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(15))
				.andExpect(jsonPath("$.nombre").value("Burguer Chorimeta"));
	}

	@Test
	void devuelve400CuandoLosDatosSonInvalidos() throws Exception {
		when(productoService.crear(any(Producto.class)))
				.thenThrow(new IllegalArgumentException("El campo 'nombre' es obligatorio"));

		mockMvc.perform(post("/api/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"categoriaId\": 1, \"precio\": 15000}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("El campo 'nombre' es obligatorio"));
	}

	@Test
	void actualizaUnProducto() throws Exception {
		when(productoService.actualizar(eq(1), any(Producto.class))).thenReturn(producto(1, "Burguer Actualizada"));

		mockMvc.perform(put("/api/productos/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "categoriaId": 1,
						  "nombre": "Burguer Actualizada",
						  "precio": 13000,
						  "disponible": true
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Burguer Actualizada"));
	}

	@Test
	void eliminaUnProductoYDevuelve204() throws Exception {
		doNothing().when(productoService).eliminar(1);

		mockMvc.perform(delete("/api/productos/1"))
				.andExpect(status().isNoContent());
	}

	private Producto producto(Integer id, String nombre) {
		return new Producto(id, 1, nombre, "Descripción", new BigDecimal("12000"), "", true);
	}
}
