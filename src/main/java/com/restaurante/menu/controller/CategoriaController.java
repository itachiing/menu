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

import com.restaurante.menu.entity.Categoria;
import com.restaurante.menu.service.CategoriaService;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@GetMapping
	public List<Categoria> listar(@RequestParam(defaultValue = "false") boolean soloActivas) {
		return categoriaService.listar(soloActivas);
	}

	@GetMapping("/{id}")
	public Categoria obtener(@PathVariable Integer id) {
		return categoriaService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Categoria crear(@RequestBody Categoria categoria) {
		return categoriaService.crear(categoria);
	}

	@PutMapping("/{id}")
	public Categoria actualizar(@PathVariable Integer id, @RequestBody Categoria categoria) {
		return categoriaService.actualizar(id, categoria);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		categoriaService.eliminar(id);
	}
}
