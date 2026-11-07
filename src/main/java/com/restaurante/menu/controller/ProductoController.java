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

import com.restaurante.menu.model.dao.CategoriaDAO;
import com.restaurante.menu.model.dao.ProductoDAO;
import com.restaurante.menu.vo.Producto;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	private final ProductoDAO productoDAO;
	private final CategoriaDAO categoriaDAO;

	public ProductoController(ProductoDAO productoDAO, CategoriaDAO categoriaDAO) {
		this.productoDAO = productoDAO;
		this.categoriaDAO = categoriaDAO;
	}

	@GetMapping
	public List<Producto> listar(@RequestParam(required = false) Integer categoriaId,
			@RequestParam(defaultValue = "false") boolean soloDisponibles) {
		return productoDAO.listar(categoriaId, soloDisponibles);
	}

	@GetMapping("/{id}")
	public Producto obtener(@PathVariable Integer id) {
		return Validaciones.encontrado(productoDAO.buscarPorId(id), "Producto");
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Producto crear(@RequestBody Producto producto) {
		validar(producto);
		return productoDAO.insertar(producto);
	}

	@PutMapping("/{id}")
	public Producto actualizar(@PathVariable Integer id, @RequestBody Producto producto) {
		obtener(id);
		producto.setId(id);
		validar(producto);
		productoDAO.actualizar(producto);
		return obtener(id);
	}

	/** Marca el producto como disponible o agotado sin borrarlo del menú. */
	@PatchMapping("/{id}/disponibilidad")
	public Producto cambiarDisponibilidad(@PathVariable Integer id, @RequestParam boolean disponible) {
		obtener(id);
		productoDAO.cambiarDisponibilidad(id, disponible);
		return obtener(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		obtener(id);
		productoDAO.eliminar(id);
	}

	private void validar(Producto producto) {
		producto.setNombre(Validaciones.texto(producto.getNombre(), "nombre", 100));
		producto.setDescripcion(Validaciones.opcional(producto.getDescripcion(), "descripcion", 300));
		producto.setImagen(Validaciones.opcional(producto.getImagen(), "imagen", 255));
		Validaciones.precio(producto.getPrecio(), "precio");
		if (producto.getCategoriaId() == null || categoriaDAO.buscarPorId(producto.getCategoriaId()).isEmpty()) {
			throw new IllegalArgumentException("La categoría indicada no existe");
		}
	}
}
