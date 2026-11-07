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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.menu.model.dao.MesaDAO;
import com.restaurante.menu.vo.Mesa;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

	private final MesaDAO mesaDAO;

	public MesaController(MesaDAO mesaDAO) {
		this.mesaDAO = mesaDAO;
	}

	@GetMapping
	public List<Mesa> listar() {
		return mesaDAO.listar();
	}

	@GetMapping("/{id}")
	public Mesa obtener(@PathVariable Integer id) {
		return Validaciones.encontrado(mesaDAO.buscarPorId(id), "Mesa");
	}

	/** Resuelve la mesa a partir del código del QR. */
	@GetMapping("/codigo/{codigo}")
	public Mesa obtenerPorCodigo(@PathVariable String codigo) {
		return Validaciones.encontrado(mesaDAO.buscarPorCodigo(codigo), "Mesa");
	}

	/** Crea la mesa; el código del QR lo genera el servidor. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mesa crear(@RequestBody Mesa mesa) {
		validar(mesa);
		return mesaDAO.insertar(mesa);
	}

	@PutMapping("/{id}")
	public Mesa actualizar(@PathVariable Integer id, @RequestBody Mesa mesa) {
		obtener(id);
		mesa.setId(id);
		validar(mesa);
		mesaDAO.actualizar(mesa);
		return obtener(id);
	}

	/** Genera un código nuevo; el QR impreso anteriormente deja de funcionar. */
	@PatchMapping("/{id}/codigo")
	public Mesa regenerarCodigo(@PathVariable Integer id) {
		obtener(id);
		mesaDAO.regenerarCodigo(id);
		return obtener(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		obtener(id);
		mesaDAO.eliminar(id);
	}

	private void validar(Mesa mesa) {
		if (mesa.getNumero() == null || mesa.getNumero() < 1) {
			throw new IllegalArgumentException("El número de mesa es obligatorio y debe ser mayor que cero");
		}
	}
}
