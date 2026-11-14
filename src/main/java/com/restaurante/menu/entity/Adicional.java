package com.restaurante.menu.entity;

import java.math.BigDecimal;

public class Adicional {

	private Integer id;
	private Integer categoriaId;
	private String nombre;
	private BigDecimal precio;
	private Boolean disponible;

	public Adicional() {
	}

	public Adicional(Integer id, Integer categoriaId, String nombre, BigDecimal precio, Boolean disponible) {
		this.id = id;
		this.categoriaId = categoriaId;
		this.nombre = nombre;
		this.precio = precio;
		this.disponible = disponible;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Integer categoriaId) {
		this.categoriaId = categoriaId;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public Boolean getDisponible() {
		return disponible;
	}

	public void setDisponible(Boolean disponible) {
		this.disponible = disponible;
	}

}

