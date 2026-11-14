package com.restaurante.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.menu.dto.NuevoPedidoRequest;
import com.restaurante.menu.dto.PedidoCompleto;
import com.restaurante.menu.repository.PedidoRepository;

/**
 * Reglas de negocio de los pedidos. El cálculo de precios y totales vive en el repositorio,
 * dentro de una sola transacción.
 */
@Service
public class PedidoService {

	private final PedidoRepository pedidoRepository;

	public PedidoService(PedidoRepository pedidoRepository) {
		this.pedidoRepository = pedidoRepository;
	}

	public PedidoCompleto crear(NuevoPedidoRequest solicitud) {
		return pedidoRepository.crear(solicitud);
	}

	/** Pedidos que el personal todavía debe atender (recibidos, en preparación o listos). */
	public List<PedidoCompleto> listarActivos() {
		return pedidoRepository.listarActivos();
	}

	public List<PedidoCompleto> listarPorEstado(String estado) {
		return pedidoRepository.listarPorEstado(estado);
	}

	public PedidoCompleto obtener(Integer id) {
		return Validaciones.encontrado(pedidoRepository.buscarCompleto(id), "Pedido");
	}

	public PedidoCompleto cambiarEstado(Integer id, String estado) {
		pedidoRepository.cambiarEstado(id, estado);
		return obtener(id);
	}
}
