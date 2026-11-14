package com.restaurante.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.menu.entity.Producto;
import com.restaurante.menu.repository.CategoriaRepository;
import com.restaurante.menu.repository.ProductoRepository;

/**
 * Reglas de negocio de los productos de la carta.
 */
@Service
public class ProductoService {

	private final ProductoRepository productoRepository;
	private final CategoriaRepository categoriaRepository;

	public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
		this.productoRepository = productoRepository;
		this.categoriaRepository = categoriaRepository;
	}

	public List<Producto> listar(Integer categoriaId, boolean soloDisponibles) {
		return productoRepository.listar(categoriaId, soloDisponibles);
	}

	public Producto obtener(Integer id) {
		return Validaciones.encontrado(productoRepository.buscarPorId(id), "Producto");
	}

	public Producto crear(Producto producto) {
		validar(producto);
		return productoRepository.insertar(producto);
	}

	public Producto actualizar(Integer id, Producto producto) {
		obtener(id);
		producto.setId(id);
		validar(producto);
		productoRepository.actualizar(producto);
		return obtener(id);
	}

	/** Marca el producto como disponible o agotado sin borrarlo del menú. */
	public Producto cambiarDisponibilidad(Integer id, boolean disponible) {
		obtener(id);
		productoRepository.cambiarDisponibilidad(id, disponible);
		return obtener(id);
	}

	/** Guarda un producto nuevo o existente (usado por el panel web). */
	public Producto guardar(Producto producto) {
		return producto.getId() == null ? crear(producto) : actualizar(producto.getId(), producto);
	}

	public void eliminar(Integer id) {
		obtener(id);
		productoRepository.eliminar(id);
	}

	private void validar(Producto producto) {
		producto.setNombre(Validaciones.texto(producto.getNombre(), "nombre", 100));
		producto.setDescripcion(Validaciones.opcional(producto.getDescripcion(), "descripcion", 300));
		producto.setImagen(Validaciones.opcional(producto.getImagen(), "imagen", 255));
		Validaciones.precio(producto.getPrecio(), "precio");
		if (producto.getCategoriaId() == null
				|| categoriaRepository.buscarPorId(producto.getCategoriaId()).isEmpty()) {
			throw new IllegalArgumentException("La categoría indicada no existe");
		}
	}
}
