package com.restaurante.menu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.restaurante.menu.service.UsuarioService;

/**
 * Garantiza que exista el administrador configurado al arrancar la aplicación.
 */
@Component
public class AdministradorInicial implements ApplicationRunner {

	private final UsuarioService usuarioService;
	private final String username;
	private final String password;

	public AdministradorInicial(UsuarioService usuarioService,
			@Value("${menu.admin.username:jonatan}") String username,
			@Value("${menu.admin.password:jonatan123}") String password) {
		this.usuarioService = usuarioService;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (username.isBlank() || password.isBlank()) {
			return;
		}

		var usuarioExistente = usuarioService.buscarPorUsername(username);
		if (usuarioExistente.isPresent()) {
			// Asegurar que la contraseña coincida con la configurada
			usuarioService.restablecerPassword(usuarioExistente.get().getId(), password);
		} else {
			// Crear el usuario administrador
			usuarioService.crearInicial(username, password, "Jonatan", "ADMIN");
		}
	}
}
