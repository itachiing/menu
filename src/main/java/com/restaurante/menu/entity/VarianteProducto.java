package com.restaurante.menu.entity;

import java.math.BigDecimal;

public class VarianteProducto {

	private Integer id;
	private Integer productoId;
	private String nombre;
	private BigDecimal precio;
	private Boolean disponible;

	public VarianteProducto() {
	}

	public VarianteProducto(Integer id, Integer productoId, String nombre, BigDecimal precio, Boolean disponible) {
		this.id = id;
		this.productoId = productoId;
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

	public Integer getProductoId() {
		return productoId;
	}

	public void setProductoId(Integer productoId) {
		this.productoId = productoId;
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

