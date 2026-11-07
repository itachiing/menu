package com.restaurante.menu.vo;

public class Categoria {

	private Integer id;
	private String nombre;
	private Integer orden;
	private Boolean activa;

	public Categoria() {
	}

	public Categoria(Integer id, String nombre, Integer orden, Boolean activa) {
		this.id = id;
		this.nombre = nombre;
		this.orden = orden;
		this.activa = activa;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

	public Boolean getActiva() {
		return activa;
	}

	public void setActiva(Boolean activa) {
		this.activa = activa;
	}

}
