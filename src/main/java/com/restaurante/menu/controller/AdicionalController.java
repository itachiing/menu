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

import com.restaurante.menu.entity.Adicional;
import com.restaurante.menu.service.AdicionalService;

/**
 * Adicionales (queso extra, tocineta, papas...). Si categoriaId es null aplican a todas las categorías.
 */
@RestController
@RequestMapping("/api/adicionales")
public class AdicionalController {

	private final AdicionalService adicionalService;

	public AdicionalController(AdicionalService adicionalService) {
		this.adicionalService = adicionalService;
	}

	/**
	 * Con categoriaId devuelve los de esa categoría más los generales; sin él devuelve todos.
	 */
	@GetMapping
	public List<Adicional> listar(@RequestParam(required = false) Integer categoriaId,
			@RequestParam(defaultValue = "false") boolean soloDisponibles) {
		return adicionalService.listar(categoriaId, soloDisponibles);
	}

	@GetMapping("/{id}")
	public Adicional obtener(@PathVariable Integer id) {
		return adicionalService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Adicional crear(@RequestBody Adicional adicional) {
		return adicionalService.crear(adicional);
	}

	@PutMapping("/{id}")
	public Adicional actualizar(@PathVariable Integer id, @RequestBody Adicional adicional) {
		return adicionalService.actualizar(id, adicional);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		adicionalService.eliminar(id);
	}
}
