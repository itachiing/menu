package com.restaurante.menu.model.dto;

import java.math.BigDecimal;
import java.util.List;

import com.restaurante.menu.vo.DetallePedido;
import com.restaurante.menu.vo.Pedido;

/**
 * Pedido con sus líneas y los nombres necesarios para mostrarlo al personal.
 */
public record PedidoCompleto(Pedido pedido, Integer mesaNumero, List<Linea> lineas) {

	/**
	 * Una línea del pedido con el nombre del producto, de la variante y los adicionales elegidos.
	 */
	public record Linea(
			DetallePedido detalle,
			String productoNombre,
			String varianteNombre,
			List<AdicionalLinea> adicionales) {
	}

	public record AdicionalLinea(Integer adicionalId, String nombre, BigDecimal precio) {
	}
}
