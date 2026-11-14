package com.restaurante.menu.entity;

import java.math.BigDecimal;

public class DetallePedidoAdicional {

	private Integer id;
	private Integer detallePedidoId;
	private Integer adicionalId;
	private BigDecimal precio;

	public DetallePedidoAdicional() {
	}

	public DetallePedidoAdicional(Integer id, Integer detallePedidoId, Integer adicionalId, BigDecimal precio) {
		this.id = id;
		this.detallePedidoId = detallePedidoId;
		this.adicionalId = adicionalId;
		this.precio = precio;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getDetallePedidoId() {
		return detallePedidoId;
	}

	public void setDetallePedidoId(Integer detallePedidoId) {
		this.detallePedidoId = detallePedidoId;
	}

	public Integer getAdicionalId() {
		return adicionalId;
	}

	public void setAdicionalId(Integer adicionalId) {
		this.adicionalId = adicionalId;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

}

