package com.restaurante.menu.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos agregados de ventas para un día, un mes o un año.
 * Los porcentajes de las gráficas ya vienen calculados para que la vista solo los pinte.
 */
public record ReporteVentas(
		String periodo,
		String tituloPeriodo,
		String rangoTexto,
		String fechaIso,
		String fechaAnteriorIso,
		String fechaSiguienteIso,
		String unidad,
		int pedidos,
		int cancelados,
		BigDecimal ventas,
		BigDecimal ticketPromedio,
		int productosVendidos,
		BigDecimal maximoSerie,
		Variacion variacionPedidos,
		Variacion variacionVentas,
		Variacion variacionTicket,
		Variacion variacionProductos,
		List<PuntoSerie> serie,
		List<PuntoSerie> actividad,
		List<ProductoVendido> topProductos,
		List<PedidoCompleto> pedidosRecientes) {

	/** Variación frente al periodo anterior. Tendencia: up, down, flat o new. */
	public record Variacion(String texto, String tendencia) {
	}

	/** Punto de la gráfica de ingresos. */
	public record PuntoSerie(
			String etiqueta,
			int pedidos,
			BigDecimal total,
			int alturaPct,
			boolean etiquetaVisible) {
	}

	/** Producto del ranking de más vendidos. */
	public record ProductoVendido(String nombre, int cantidad, BigDecimal total, int barraPct) {
	}

	public boolean isHaySerie() {
		return serie.stream().anyMatch(punto -> punto.pedidos() > 0);
	}

	public boolean isHayProductos() {
		return !topProductos.isEmpty();
	}
}
