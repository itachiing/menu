package com.restaurante.menu.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.menu.dto.PedidoCompleto;
import com.restaurante.menu.entity.Pedido;
import com.restaurante.menu.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

	@Mock
	private PedidoRepository pedidoRepository;

	@InjectMocks
	private PedidoService pedidoService;

	@Test
	void devuelveElPedidoCompleto() {
		PedidoCompleto pedido = pedidoCompleto();
		when(pedidoRepository.buscarCompleto(1)).thenReturn(Optional.of(pedido));

		assertSame(pedido, pedidoService.obtener(1));
	}

	@Test
	void lanzaNoSuchElementCuandoElPedidoNoExiste() {
		when(pedidoRepository.buscarCompleto(99)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> pedidoService.obtener(99));
	}

	@Test
	void cambiaElEstadoYDevuelveElPedidoActualizado() {
		PedidoCompleto pedido = pedidoCompleto();
		when(pedidoRepository.buscarCompleto(1)).thenReturn(Optional.of(pedido));

		pedidoService.cambiarEstado(1, "EN_PREPARACION");

		verify(pedidoRepository).cambiarEstado(1, "EN_PREPARACION");
		verify(pedidoRepository).buscarCompleto(1);
	}

	private PedidoCompleto pedidoCompleto() {
		Pedido pedido = new Pedido(1, 3, "MESA", "Cliente", "3001234567", null,
				LocalDateTime.now(), "RECIBIDO", new BigDecimal("12000"), null);
		return new PedidoCompleto(pedido, 3, List.of());
	}
}
