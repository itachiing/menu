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

import com.restaurante.menu.entity.Mesa;
import com.restaurante.menu.service.MesaService;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

	private final MesaService mesaService;

	public MesaController(MesaService mesaService) {
		this.mesaService = mesaService;
	}

	@GetMapping
	public List<Mesa> listar() {
		return mesaService.listar();
	}

	@GetMapping("/{id}")
	public Mesa obtener(@PathVariable Integer id) {
		return mesaService.obtener(id);
	}

	/** Resuelve la mesa a partir del código del QR. */
	@GetMapping("/codigo/{codigo}")
	public Mesa obtenerPorCodigo(@PathVariable String codigo) {
		return mesaService.obtenerPorCodigo(codigo);
	}

	/** Crea la mesa; el código del QR lo genera el servidor. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mesa crear(@RequestBody Mesa mesa) {
		return mesaService.crear(mesa);
	}

	@PutMapping("/{id}")
	public Mesa actualizar(@PathVariable Integer id, @RequestBody Mesa mesa) {
		return mesaService.actualizar(id, mesa);
	}

	/** Genera un código nuevo; el QR impreso anteriormente deja de funcionar. */
	@PatchMapping("/{id}/codigo")
	public Mesa regenerarCodigo(@PathVariable Integer id) {
		return mesaService.regenerarCodigo(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		mesaService.eliminar(id);
	}
}
