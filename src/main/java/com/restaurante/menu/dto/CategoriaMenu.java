package com.restaurante.menu.dto;

import java.util.List;

import com.restaurante.menu.entity.Categoria;
import com.restaurante.menu.entity.Producto;
import com.restaurante.menu.entity.VarianteProducto;

/**
 * Categoría con sus productos disponibles, tal como se muestra al cliente en el menú.
 */
public record CategoriaMenu(Categoria categoria, List<ProductoMenu> productos) {

	/**
	 * Producto con sus variantes disponibles (tamaños o porciones); la lista va vacía si no tiene.
	 */
	public record ProductoMenu(Producto producto, List<VarianteProducto> variantes) {
	}
}
