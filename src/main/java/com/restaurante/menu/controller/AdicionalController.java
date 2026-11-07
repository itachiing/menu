package com.restaurante.menu.controller;

import java.math.BigDecimal;
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

import com.restaurante.menu.model.dao.AdicionalDAO;
import com.restaurante.menu.model.dao.CategoriaDAO;
import com.restaurante.menu.vo.Adicional;

/**
 * Adicionales (queso extra, tocineta, papas...). Si categoriaId es null aplican a todas las categorías.
 */
@RestController
@RequestMapping("/api/adicionales")
public class AdicionalController {

	private final AdicionalDAO adicionalDAO;
	private final CategoriaDAO categoriaDAO;

	public AdicionalController(AdicionalDAO adicionalDAO, CategoriaDAO categoriaDAO) {
		this.adicionalDAO = adicionalDAO;
		this.categoriaDAO = categoriaDAO;
	}

	/**
	 * Con categoriaId devuelve los de esa categoría más los generales; sin él devuelve todos.
	 */
	@GetMapping
	public List<Adicional> listar(@RequestParam(required = false) Integer categoriaId,
			@RequestParam(defaultValue = "false") boolean soloDisponibles) {
		return adicionalDAO.listar(categoriaId, soloDisponibles);
	}

	@GetMapping("/{id}")
	public Adicional obtener(@PathVariable Integer id) {
		return Validaciones.encontrado(adicionalDAO.buscarPorId(id), "Adicional");
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Adicional crear(@RequestBody Adicional adicional) {
		validar(adicional);
		return adicionalDAO.insertar(adicional);
	}

	@PutMapping("/{id}")
	public Adicional actualizar(@PathVariable Integer id, @RequestBody Adicional adicional) {
		obtener(id);
		adicional.setId(id);
		validar(adicional);
		adicionalDAO.actualizar(adicional);
		return obtener(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		obtener(id);
		adicionalDAO.eliminar(id);
	}

	private void validar(Adicional adicional) {
		adicional.setNombre(Validaciones.texto(adicional.getNombre(), "nombre", 80));
		if (adicional.getPrecio() == null) {
			adicional.setPrecio(BigDecimal.ZERO);
		}
		Validaciones.precio(adicional.getPrecio(), "precio");
		if (adicional.getCategoriaId() != null
				&& categoriaDAO.buscarPorId(adicional.getCategoriaId()).isEmpty()) {
			throw new IllegalArgumentException("La categoría indicada no existe");
		}
	}
}
