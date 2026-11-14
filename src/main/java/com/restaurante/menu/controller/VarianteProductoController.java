package com.restaurante.menu.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.menu.entity.VarianteProducto;
import com.restaurante.menu.service.VarianteProductoService;

/**
 * Variantes de un producto (tamaños o porciones, por ejemplo picada para 2 o para 4 personas).
 */
@RestController
@RequestMapping("/api")
public class VarianteProductoController {

	private final VarianteProductoService varianteService;

	public VarianteProductoController(VarianteProductoService varianteService) {
		this.varianteService = varianteService;
	}

	@GetMapping("/productos/{productoId}/variantes")
	public List<VarianteProducto> listarPorProducto(@PathVariable Integer productoId,
			@RequestParam(defaultValue = "false") boolean soloDisponibles) {
		return varianteService.listarPorProducto(productoId, soloDisponibles);
	}

	@PostMapping("/productos/{productoId}/variantes")
	@ResponseStatus(HttpStatus.CREATED)
	public VarianteProducto crear(@PathVariable Integer productoId, @RequestBody VarianteProducto variante) {
		return varianteService.crear(productoId, variante);
	}

	@GetMapping("/variantes/{id}")
	public VarianteProducto obtener(@PathVariable Integer id) {
		return varianteService.obtener(id);
	}

	@PutMapping("/variantes/{id}")
	public VarianteProducto actualizar(@PathVariable Integer id, @RequestBody VarianteProducto variante) {
		return varianteService.actualizar(id, variante);
	}

	@DeleteMapping("/variantes/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		varianteService.eliminar(id);
	}
}
