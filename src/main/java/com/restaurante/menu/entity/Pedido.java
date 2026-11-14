package com.restaurante.menu.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pedido {

	private Integer id;
	private Integer mesaId;
	private String tipoPedido;
	private String nombreCliente;
	private String telefono;
	private String direccion;
	private LocalDateTime fechaHora;
	private String estado;
	private BigDecimal total;
	private String observaciones;

	public Pedido() {
	}

	public Pedido(Integer id, Integer mesaId, String tipoPedido, String nombreCliente, String telefono, String direccion, LocalDateTime fechaHora, String estado, BigDecimal total, String observaciones) {
		this.id = id;
		this.mesaId = mesaId;
		this.tipoPedido = tipoPedido;
		this.nombreCliente = nombreCliente;
		this.telefono = telefono;
		this.direccion = direccion;
		this.fechaHora = fechaHora;
		this.estado = estado;
		this.total = total;
		this.observaciones = observaciones;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getMesaId() {
		return mesaId;
	}

	public void setMesaId(Integer mesaId) {
		this.mesaId = mesaId;
	}

	public String getTipoPedido() {
		return tipoPedido;
	}

	public void setTipoPedido(String tipoPedido) {
		this.tipoPedido = tipoPedido;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

}

