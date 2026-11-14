package com.restaurante.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.menu.entity.Categoria;
import com.restaurante.menu.entity.Producto;
import com.restaurante.menu.repository.CategoriaRepository;
import com.restaurante.menu.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

	@Mock
	private ProductoRepository productoRepository;

	@Mock
	private CategoriaRepository categoriaRepository;

	@InjectMocks
	private ProductoService productoService;

	@Test
	void creaUnProductoCuandoLaCategoriaExiste() {
		when(categoriaRepository.buscarPorId(1)).thenReturn(Optional.of(new Categoria(1, "Hamburguesas", 1, true)));
		when(productoRepository.insertar(any(Producto.class))).thenAnswer(invocacion -> {
			Producto producto = invocacion.getArgument(0);
			producto.setId(10);
			return producto;
		});

		Producto creado = productoService.crear(producto("Burguer al Carbón"));

		assertEquals(10, creado.getId());
		assertEquals("Burguer al Carbón", creado.getNombre());
		verify(productoRepository).insertar(any(Producto.class));
	}

	@Test
	void rechazaUnProductoConCategoriaInexistente() {
		when(categoriaRepository.buscarPorId(99)).thenReturn(Optional.empty());

		Producto producto = producto("Producto inválido");
		producto.setCategoriaId(99);

		assertThrows(IllegalArgumentException.class, () -> productoService.crear(producto));
		verify(productoRepository, never()).insertar(any(Producto.class));
	}

	@Test
	void rechazaUnProductoConPrecioNegativo() {
		Producto producto = producto("Precio inválido");
		producto.setPrecio(new BigDecimal("-1"));

		assertThrows(IllegalArgumentException.class, () -> productoService.crear(producto));
		verify(productoRepository, never()).insertar(any(Producto.class));
	}

	@Test
	void lanzaNoSuchElementCuandoElProductoNoExiste() {
		when(productoRepository.buscarPorId(99)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> productoService.obtener(99));
	}

	@Test
	void cambiaLaDisponibilidadDeUnProductoExistente() {
		Producto producto = producto("Burguer al Carbón");
		producto.setId(1);
		when(productoRepository.buscarPorId(1)).thenReturn(Optional.of(producto));
		when(productoRepository.cambiarDisponibilidad(1, false)).thenReturn(true);

		productoService.cambiarDisponibilidad(1, false);

		verify(productoRepository).cambiarDisponibilidad(1, false);
	}

	private Producto producto(String nombre) {
		return new Producto(null, 1, nombre, "Descripción", new BigDecimal("12000"), "", true);
	}
}
