package com.restaurante.menu.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.restaurante.menu.dto.CategoriaMenu;
import com.restaurante.menu.entity.VarianteProducto;
import com.restaurante.menu.repository.CategoriaRepository;
import com.restaurante.menu.repository.ProductoRepository;
import com.restaurante.menu.repository.VarianteProductoRepository;

/**
 * Arma el menú que ve el cliente: categorías activas con sus productos y variantes disponibles.
 */
@Service
public class MenuService {

	private final CategoriaRepository categoriaRepository;
	private final ProductoRepository productoRepository;
	private final VarianteProductoRepository varianteRepository;

	public MenuService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
			VarianteProductoRepository varianteRepository) {
		this.categoriaRepository = categoriaRepository;
		this.productoRepository = productoRepository;
		this.varianteRepository = varianteRepository;
	}

	public List<CategoriaMenu> obtenerMenu() {
		Map<Integer, List<VarianteProducto>> variantesPorProducto = varianteRepository.listarDisponibles().stream()
				.collect(Collectors.groupingBy(VarianteProducto::getProductoId));

		Map<Integer, List<CategoriaMenu.ProductoMenu>> productosPorCategoria = productoRepository.listar(null, true)
				.stream()
				.map(p -> new CategoriaMenu.ProductoMenu(p,
						variantesPorProducto.getOrDefault(p.getId(), List.of())))
				.collect(Collectors.groupingBy(pm -> pm.producto().getCategoriaId(),
						LinkedHashMap::new, Collectors.toList()));

		return categoriaRepository.listar(true).stream()
				.filter(c -> productosPorCategoria.containsKey(c.getId()))
				.map(c -> new CategoriaMenu(c, productosPorCategoria.get(c.getId())))
				.toList();
	}
}
