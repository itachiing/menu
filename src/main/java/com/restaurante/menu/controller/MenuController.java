package com.restaurante.menu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.menu.dto.CategoriaMenu;
import com.restaurante.menu.service.MenuService;

/**
 * Menú que ve el cliente: categorías activas con sus productos y variantes disponibles.
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

	private final MenuService menuService;

	public MenuController(MenuService menuService) {
		this.menuService = menuService;
	}

	@GetMapping
	public List<CategoriaMenu> obtenerMenu() {
		return menuService.obtenerMenu();
	}
}
