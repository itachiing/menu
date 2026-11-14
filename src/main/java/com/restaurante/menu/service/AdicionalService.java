package com.restaurante.menu.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.menu.entity.Adicional;
import com.restaurante.menu.repository.AdicionalRepository;
import com.restaurante.menu.repository.CategoriaRepository;

/**
 * Adicionales (queso extra, tocineta, papas...). Si categoriaId es null aplican a todas las categorías.
 */
@Service
public class AdicionalService {

	private final AdicionalRepository adicionalRepository;
	private final CategoriaRepository categoriaRepository;

	public AdicionalService(AdicionalRepository adicionalRepository, CategoriaRepository categoriaRepository) {
		this.adicionalRepository = adicionalRepository;
		this.categoriaRepository = categoriaRepository;
	}

	public List<Adicional> listar(Integer categoriaId, boolean soloDisponibles) {
		return adicionalRepository.listar(categoriaId, soloDisponibles);
	}

	public Adicional obtener(Integer id) {
		return Validaciones.encontrado(adicionalRepository.buscarPorId(id), "Adicional");
	}

	public Adicional crear(Adicional adicional) {
		validar(adicional);
		return adicionalRepository.insertar(adicional);
	}

	public Adicional actualizar(Integer id, Adicional adicional) {
		obtener(id);
		adicional.setId(id);
		validar(adicional);
		adicionalRepository.actualizar(adicional);
		return obtener(id);
	}

	/** Guarda un adicional nuevo o existente (usado por el panel web). */
	public Adicional guardar(Adicional adicional) {
		return adicional.getId() == null ? crear(adicional) : actualizar(adicional.getId(), adicional);
	}

	public void eliminar(Integer id) {
		obtener(id);
		adicionalRepository.eliminar(id);
	}

	private void validar(Adicional adicional) {
		adicional.setNombre(Validaciones.texto(adicional.getNombre(), "nombre", 80));
		if (adicional.getPrecio() == null) {
			adicional.setPrecio(BigDecimal.ZERO);
		}
		Validaciones.precio(adicional.getPrecio(), "precio");
		if (adicional.getCategoriaId() != null
				&& categoriaRepository.buscarPorId(adicional.getCategoriaId()).isEmpty()) {
			throw new IllegalArgumentException("La categoría indicada no existe");
		}
	}
}
