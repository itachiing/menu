package com.restaurante.menu.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.menu.entity.Producto;
import com.restaurante.menu.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	private final ProductoService productoService;

	public ProductoController(ProductoService productoService) {
		this.productoService = productoService;
	}

	@GetMapping
	public List<Producto> listar(@RequestParam(required = false) Integer categoriaId,
			@RequestParam(defaultValue = "false") boolean soloDisponibles) {
		return productoService.listar(categoriaId, soloDisponibles);
	}

	@GetMapping("/{id}")
	public Producto obtener(@PathVariable Integer id) {
		return productoService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Producto crear(@RequestBody Producto producto) {
		return productoService.crear(producto);
	}

	@PutMapping("/{id}")
	public Producto actualizar(@PathVariable Integer id, @RequestBody Producto producto) {
		return productoService.actualizar(id, producto);
	}

	/** Marca el producto como disponible o agotado sin borrarlo del menú. */
	@PatchMapping("/{id}/disponibilidad")
	public Producto cambiarDisponibilidad(@PathVariable Integer id, @RequestParam boolean disponible) {
		return productoService.cambiarDisponibilidad(id, disponible);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		productoService.eliminar(id);
	}
}
