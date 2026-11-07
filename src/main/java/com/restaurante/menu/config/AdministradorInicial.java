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
			@Value("${menu.admin.username:}") String username,
			@Value("${menu.admin.password:}") String password) {
		this.usuarioDAO = usuarioDAO;
		this.passwordEncoder = passwordEncoder;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (username.isBlank() || password.isBlank() || usuarioDAO.buscarPorUsername(username).isPresent()) {
			return;
		}
		if (password.length() < 10) {
			throw new IllegalStateException("MENU_ADMIN_PASSWORD debe tener al menos 10 caracteres.");
		}
		usuarioDAO.insertar(new Usuario(null, username, passwordEncoder.encode(password),
				"Administrador", "ADMIN", true));
	}
}