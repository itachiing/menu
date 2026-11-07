package com.restaurante.menu.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.menu.model.dao.CategoriaDAO;
import com.restaurante.menu.model.dao.ProductoDAO;
import com.restaurante.menu.model.dao.VarianteProductoDAO;
import com.restaurante.menu.model.dto.CategoriaMenu;
import com.restaurante.menu.vo.VarianteProducto;

/**
 * Menú que ve el cliente: categorías activas con sus productos y variantes disponibles.
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

	private final CategoriaDAO categoriaDAO;
	private final ProductoDAO productoDAO;
	private final VarianteProductoDAO varianteDAO;

	public MenuController(CategoriaDAO categoriaDAO, ProductoDAO productoDAO, VarianteProductoDAO varianteDAO) {
		this.categoriaDAO = categoriaDAO;
		this.productoDAO = productoDAO;
		this.varianteDAO = varianteDAO;
	}

	@GetMapping
	public List<CategoriaMenu> obtenerMenu() {
		Map<Integer, List<VarianteProducto>> variantesPorProducto = varianteDAO.listarDisponibles().stream()
				.collect(Collectors.groupingBy(VarianteProducto::getProductoId));

		Map<Integer, List<CategoriaMenu.ProductoMenu>> productosPorCategoria = productoDAO.listar(null, true)
				.stream()
				.map(p -> new CategoriaMenu.ProductoMenu(p, variantesPorProducto.getOrDefault(p.getId(), List.of())))
				.collect(Collectors.groupingBy(pm -> pm.producto().getCategoriaId(),
						LinkedHashMap::new, Collectors.toList()));

		return categoriaDAO.listar(true).stream()
				.filter(c -> productosPorCategoria.containsKey(c.getId()))
				.map(c -> new CategoriaMenu(c, productosPorCategoria.get(c.getId())))
				.toList();
	}
}
