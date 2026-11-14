package com.restaurante.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.menu.entity.VarianteProducto;
import com.restaurante.menu.repository.ProductoRepository;
import com.restaurante.menu.repository.VarianteProductoRepository;

/**
 * Variantes de un producto (tamaños o porciones, por ejemplo picada para 2 o para 4 personas).
 */
@Service
public class VarianteProductoService {

	private final VarianteProductoRepository varianteRepository;
	private final ProductoRepository productoRepository;

	public VarianteProductoService(VarianteProductoRepository varianteRepository,
			ProductoRepository productoRepository) {
		this.varianteRepository = varianteRepository;
		this.productoRepository = productoRepository;
	}

	public List<VarianteProducto> listarPorProducto(Integer productoId, boolean soloDisponibles) {
		Validaciones.encontrado(productoRepository.buscarPorId(productoId), "Producto");
		return varianteRepository.listarPorProducto(productoId, soloDisponibles);
	}

	public List<VarianteProducto> listarDisponibles() {
		return varianteRepository.listarDisponibles();
	}

	public VarianteProducto obtener(Integer id) {
		return Validaciones.encontrado(varianteRepository.buscarPorId(id), "Variante");
	}

	public VarianteProducto crear(Integer productoId, VarianteProducto variante) {
		Validaciones.encontrado(productoRepository.buscarPorId(productoId), "Producto");
		variante.setProductoId(productoId);
		validar(variante);
		return varianteRepository.insertar(variante);
	}

	public VarianteProducto actualizar(Integer id, VarianteProducto variante) {
		obtener(id);
		variante.setId(id);
		validar(variante);
		varianteRepository.actualizar(variante);
		return obtener(id);
	}

	/** Guarda una variante nueva o existente (usado por el panel web). */
	public VarianteProducto guardar(VarianteProducto variante) {
		return variante.getId() == null
				? crear(variante.getProductoId(), variante)
				: actualizar(variante.getId(), variante);
	}

	public void eliminar(Integer id) {
		obtener(id);
		varianteRepository.eliminar(id);
	}

	private void validar(VarianteProducto variante) {
		variante.setNombre(Validaciones.texto(variante.getNombre(), "nombre", 60));
		Validaciones.precio(variante.getPrecio(), "precio");
	}
}
