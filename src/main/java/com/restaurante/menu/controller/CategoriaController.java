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

import com.restaurante.menu.model.dao.CategoriaDAO;
import com.restaurante.menu.vo.Categoria;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaDAO categoriaDAO;

	public CategoriaController(CategoriaDAO categoriaDAO) {
		this.categoriaDAO = categoriaDAO;
	}

	@GetMapping
	public List<Categoria> listar(@RequestParam(defaultValue = "false") boolean soloActivas) {
		return categoriaDAO.listar(soloActivas);
	}

	@GetMapping("/{id}")
	public Categoria obtener(@PathVariable Integer id) {
		return Validaciones.encontrado(categoriaDAO.buscarPorId(id), "Categoría");
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Categoria crear(@RequestBody Categoria categoria) {
		categoria.setNombre(Validaciones.texto(categoria.getNombre(), "nombre", 60));
		return categoriaDAO.insertar(categoria);
	}

	@PutMapping("/{id}")
	public Categoria actualizar(@PathVariable Integer id, @RequestBody Categoria categoria) {
		obtener(id);
		categoria.setId(id);
		categoria.setNombre(Validaciones.texto(categoria.getNombre(), "nombre", 60));
		categoriaDAO.actualizar(categoria);
		return obtener(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		obtener(id);
		categoriaDAO.eliminar(id);
	}
}
