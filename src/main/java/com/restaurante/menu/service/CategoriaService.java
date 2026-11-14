package com.restaurante.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.menu.entity.Categoria;
import com.restaurante.menu.repository.CategoriaRepository;

/**
 * Reglas de negocio de las categorías del menú.
 */
@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;

	public CategoriaService(CategoriaRepository categoriaRepository) {
		this.categoriaRepository = categoriaRepository;
	}

	public List<Categoria> listar(boolean soloActivas) {
		return categoriaRepository.listar(soloActivas);
	}

	public Categoria obtener(Integer id) {
		return Validaciones.encontrado(categoriaRepository.buscarPorId(id), "Categoría");
	}

	public Categoria crear(Categoria categoria) {
		categoria.setNombre(Validaciones.texto(categoria.getNombre(), "nombre", 60));
		return categoriaRepository.insertar(categoria);
	}

	public Categoria actualizar(Integer id, Categoria categoria) {
		obtener(id);
		categoria.setId(id);
		categoria.setNombre(Validaciones.texto(categoria.getNombre(), "nombre", 60));
		categoriaRepository.actualizar(categoria);
		return obtener(id);
	}

	/** Guarda una categoría nueva o existente (usado por el panel web). */
	public Categoria guardar(Categoria categoria) {
		return categoria.getId() == null ? crear(categoria) : actualizar(categoria.getId(), categoria);
	}

	public void eliminar(Integer id) {
		obtener(id);
		categoriaRepository.eliminar(id);
	}

	/** Comprueba que la categoría exista; se usa desde los servicios de producto y adicional. */
	public void exigirExistente(Integer id) {
		if (id == null || categoriaRepository.buscarPorId(id).isEmpty()) {
			throw new IllegalArgumentException("La categoría indicada no existe");
		}
	}
}
