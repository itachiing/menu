package com.restaurante.menu.entity;

import java.math.BigDecimal;

public class DetallePedido {

	private Integer id;
	private Integer pedidoId;
	private Integer productoId;
	private Integer varianteId;
	private Integer cantidad;
	private BigDecimal precioUnitario;
	private String observacion;

	public DetallePedido() {
	}

	public DetallePedido(Integer id, Integer pedidoId, Integer productoId, Integer varianteId, Integer cantidad, BigDecimal precioUnitario, String observacion) {
		this.id = id;
		this.pedidoId = pedidoId;
		this.productoId = productoId;
		this.varianteId = varianteId;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
		this.observacion = observacion;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPedidoId() {
		return pedidoId;
	}

	public void setPedidoId(Integer pedidoId) {
		this.pedidoId = pedidoId;
	}

	public Integer getProductoId() {
		return productoId;
	}

	public void setProductoId(Integer productoId) {
		this.productoId = productoId;
	}

	public Integer getVarianteId() {
		return varianteId;
	}

	public void setVarianteId(Integer varianteId) {
		this.varianteId = varianteId;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}

}

