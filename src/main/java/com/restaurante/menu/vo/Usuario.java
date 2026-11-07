package com.restaurante.menu.vo;

public class Usuario {

	private Integer id;
	private String username;
	private String password;
	private String nombre;
	private String rol;
	private Boolean activo;

	public Usuario() {
	}

	public Usuario(Integer id, String username, String password, String nombre, String rol, Boolean activo) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.nombre = nombre;
		this.rol = rol;
		this.activo = activo;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

}
