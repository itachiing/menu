package com.restaurante.menu.config;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class DatosDeVista {

	@ModelAttribute
	public void agregarPermisos(Model model, Authentication authentication) {
		boolean autenticado = authentication != null && authentication.isAuthenticated()
				&& authentication.getAuthorities().stream()
						.noneMatch(authority -> "ROLE_ANONYMOUS".equals(authority.getAuthority()));
		model.addAttribute("isAuthenticated", autenticado);
		model.addAttribute("nombreUsuario", autenticado ? authentication.getName() : "Invitado");
		model.addAttribute("isAdmin", autenticado && tieneRol(authentication, "ROLE_ADMIN"));
		model.addAttribute("isCocina", autenticado && tieneRol(authentication, "ROLE_COCINA"));
		model.addAttribute("isMesero", autenticado && tieneRol(authentication, "ROLE_MESERO"));
	}

	private boolean tieneRol(Authentication authentication, String rol) {
		return authentication.getAuthorities().stream()
				.anyMatch(authority -> rol.equals(authority.getAuthority()));
	}
}