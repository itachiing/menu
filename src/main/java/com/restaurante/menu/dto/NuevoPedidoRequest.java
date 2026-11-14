package com.restaurante.menu.dto;

import java.util.List;

/**
 * Pedido que envía el cliente. No incluye precios: el servidor los toma de la base de datos
 * y calcula el total.
 *
 * @param tipoPedido  MESA (por defecto), LLEVAR o DOMICILIO
 * @param mesaCodigo  código del QR de la mesa; obligatorio cuando el tipo es MESA
 */
public record NuevoPedidoRequest(
		String tipoPedido,
		String mesaCodigo,
		String nombreCliente,
		String telefono,
		String direccion,
		String observaciones,
		List<Item> items) {

	/**
	 * Un producto del pedido.
	 *
	 * @param varianteId  obligatorio si el producto tiene variantes (por ejemplo, picada para 2 o 4)
	 * @param adicionalIds ids de los adicionales elegidos (queso extra, tocineta...)
	 */
	public record Item(
			Integer productoId,
			Integer varianteId,
			Integer cantidad,
			String observacion,
			List<Integer> adicionalIds) {
	}
}
