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

import com.restaurante.menu.model.dao.ProductoDAO;
import com.restaurante.menu.model.dao.VarianteProductoDAO;
import com.restaurante.menu.vo.VarianteProducto;

/**
 * Variantes de un producto (tamaños o porciones, por ejemplo picada para 2 o para 4 personas).
 */
@RestController
@RequestMapping("/api")
public class VarianteProductoController {

	private final VarianteProductoDAO varianteDAO;
	private final ProductoDAO productoDAO;

	public VarianteProductoController(VarianteProductoDAO varianteDAO, ProductoDAO productoDAO) {
		this.varianteDAO = varianteDAO;
		this.productoDAO = productoDAO;
	}

	@GetMapping("/productos/{productoId}/variantes")
	public List<VarianteProducto> listarPorProducto(@PathVariable Integer productoId,
			@RequestParam(defaultValue = "false") boolean soloDisponibles) {
		Validaciones.encontrado(productoDAO.buscarPorId(productoId), "Producto");
		return varianteDAO.listarPorProducto(productoId, soloDisponibles);
	}

	@PostMapping("/productos/{productoId}/variantes")
	@ResponseStatus(HttpStatus.CREATED)
	public VarianteProducto crear(@PathVariable Integer productoId, @RequestBody VarianteProducto variante) {
		Validaciones.encontrado(productoDAO.buscarPorId(productoId), "Producto");
		variante.setProductoId(productoId);
		validar(variante);
		return varianteDAO.insertar(variante);
	}

	@GetMapping("/variantes/{id}")
	public VarianteProducto obtener(@PathVariable Integer id) {
		return Validaciones.encontrado(varianteDAO.buscarPorId(id), "Variante");
	}

	@PutMapping("/variantes/{id}")
	public VarianteProducto actualizar(@PathVariable Integer id, @RequestBody VarianteProducto variante) {
		obtener(id);
		variante.setId(id);
		validar(variante);
		varianteDAO.actualizar(variante);
		return obtener(id);
	}

	@DeleteMapping("/variantes/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		obtener(id);
		varianteDAO.eliminar(id);
	}

	private void validar(VarianteProducto variante) {
		variante.setNombre(Validaciones.texto(variante.getNombre(), "nombre", 60));
		Validaciones.precio(variante.getPrecio(), "precio");
	}
}
