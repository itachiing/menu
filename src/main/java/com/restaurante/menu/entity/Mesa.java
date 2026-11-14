package com.restaurante.menu.entity;

public class Mesa {

	private Integer id;
	private Integer numero;
	private String codigo;
	private Boolean activa;

	public Mesa() {
	}

	public Mesa(Integer id, Integer numero, String codigo, Boolean activa) {
		this.id = id;
		this.numero = numero;
		this.codigo = codigo;
		this.activa = activa;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Boolean getActiva() {
		return activa;
	}

	public void setActiva(Boolean activa) {
		this.activa = activa;
	}

}

