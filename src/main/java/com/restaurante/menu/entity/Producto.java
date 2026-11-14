package com.restaurante.menu.entity;

import java.math.BigDecimal;

public class Producto {

	private Integer id;
	private Integer categoriaId;
	private String nombre;
	private String descripcion;
	private BigDecimal precio;
	private String imagen;
	private Boolean disponible;

	public Producto() {
	}

	public Producto(Integer id, Integer categoriaId, String nombre, String descripcion, BigDecimal precio, String imagen, Boolean disponible) {
		this.id = id;
		this.categoriaId = categoriaId;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
		this.imagen = imagen;
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

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}

	public Boolean getDisponible() {
		return disponible;
	}

	public void setDisponible(Boolean disponible) {
		this.disponible = disponible;
	}

}

