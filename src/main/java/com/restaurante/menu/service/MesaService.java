package com.restaurante.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.menu.entity.Mesa;
import com.restaurante.menu.repository.MesaRepository;

/**
 * Reglas de negocio de las mesas y sus códigos QR.
 */
@Service
public class MesaService {

	private final MesaRepository mesaRepository;

	public MesaService(MesaRepository mesaRepository) {
		this.mesaRepository = mesaRepository;
	}

	public List<Mesa> listar() {
		return mesaRepository.listar();
	}

	public Mesa obtener(Integer id) {
		return Validaciones.encontrado(mesaRepository.buscarPorId(id), "Mesa");
	}

	/** Resuelve la mesa a partir del código del QR. */
	public Mesa obtenerPorCodigo(String codigo) {
		return Validaciones.encontrado(mesaRepository.buscarPorCodigo(codigo), "Mesa");
	}

	public Mesa crear(Mesa mesa) {
		validar(mesa);
		return mesaRepository.insertar(mesa);
	}

	public Mesa actualizar(Integer id, Mesa mesa) {
		obtener(id);
		mesa.setId(id);
		validar(mesa);
		mesaRepository.actualizar(mesa);
		return obtener(id);
	}

	/** Genera un código nuevo; el QR impreso anteriormente deja de funcionar. */
	public Mesa regenerarCodigo(Integer id) {
		obtener(id);
		mesaRepository.regenerarCodigo(id);
		return obtener(id);
	}

	/** Guarda una mesa nueva o existente (usado por el panel web). */
	public Mesa guardar(Mesa mesa) {
		return mesa.getId() == null ? crear(mesa) : actualizar(mesa.getId(), mesa);
	}

	public void eliminar(Integer id) {
		obtener(id);
		mesaRepository.eliminar(id);
	}

	private void validar(Mesa mesa) {
		if (mesa.getNumero() == null || mesa.getNumero() < 1) {
			throw new IllegalArgumentException("El número de mesa es obligatorio y debe ser mayor que cero");
		}
	}
}
