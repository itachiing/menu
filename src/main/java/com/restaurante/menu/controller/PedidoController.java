package com.restaurante.menu.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.menu.model.dao.PedidoDAO;
import com.restaurante.menu.model.dto.NuevoPedidoRequest;
import com.restaurante.menu.model.dto.PedidoCompleto;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	private final PedidoDAO pedidoDAO;

	public PedidoController(PedidoDAO pedidoDAO) {
		this.pedidoDAO = pedidoDAO;
	}

	/** El cliente envía su pedido; el servidor calcula precios y total. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PedidoCompleto crear(@RequestBody NuevoPedidoRequest solicitud) {
		return pedidoDAO.crear(solicitud);
	}

	/**
	 * Sin parámetro devuelve los pedidos por atender (recibidos, en preparación y listos);
	 * con estado devuelve los de ese estado.
	 */
	@GetMapping
	public List<PedidoCompleto> listar(@RequestParam(required = false) String estado) {
		if (estado == null || estado.isBlank()) {
			return pedidoDAO.listarActivos();
		}
		return pedidoDAO.listarPorEstado(estado);
	}

	@GetMapping("/{id}")
	public PedidoCompleto obtener(@PathVariable Integer id) {
		return Validaciones.encontrado(pedidoDAO.buscarCompleto(id), "Pedido");
	}

	/** Cambia el estado: RECIBIDO, EN_PREPARACION, LISTO, ENTREGADO o CANCELADO. */
	@PatchMapping("/{id}/estado")
	public PedidoCompleto cambiarEstado(@PathVariable Integer id, @RequestParam String estado) {
		pedidoDAO.cambiarEstado(id, estado);
		return obtener(id);
	}
}
