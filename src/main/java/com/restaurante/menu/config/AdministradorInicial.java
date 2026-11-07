package com.restaurante.menu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.restaurante.menu.model.dao.UsuarioDAO;
import com.restaurante.menu.vo.Usuario;

@Component
public class AdministradorInicial implements ApplicationRunner {

	private final UsuarioDAO usuarioDAO;
	private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
	private final String username;
	private final String password;

	public AdministradorInicial(UsuarioDAO usuarioDAO,
			org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
			@Value("${menu.admin.username:jonatan}") String username,
			@Value("${menu.admin.password:jonatan123}") String password) {
		this.usuarioDAO = usuarioDAO;
		this.passwordEncoder = passwordEncoder;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (username.isBlank() || password.isBlank()) {
			return;
		}
		
		var usuarioExistente = usuarioDAO.buscarPorUsername(username);
		if (usuarioExistente.isPresent()) {
			// Asegurar que la contraseña coincida con la configurada
			usuarioDAO.cambiarPassword(usuarioExistente.get().getId(), passwordEncoder.encode(password));
		} else {
			// Crear el usuario administrador
			usuarioDAO.insertar(new Usuario(null, username, passwordEncoder.encode(password),
					"Jonatan", "ADMIN", true));
		}
	}
}
