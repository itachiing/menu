package com.restaurante.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.menu.dto.ReporteVentas;
import com.restaurante.menu.repository.PedidoRepository;
import com.restaurante.menu.repository.ReporteRepository;
import com.restaurante.menu.repository.ReporteRepository.FilaAgrupada;
import com.restaurante.menu.repository.ReporteRepository.FilaProducto;
import com.restaurante.menu.repository.ReporteRepository.Resumen;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

	private static final LocalDate DIA = LocalDate.of(2026, 11, 8);
	private static final LocalDateTime INICIO = DIA.atStartOfDay();
	private static final LocalDateTime FIN = INICIO.plusDays(1);

	@Mock
	private ReporteRepository reporteRepository;

	@Mock
	private PedidoRepository pedidoRepository;

	@InjectMocks
	private ReporteService reporteService;

	@Test
	void elReporteDiarioRellenaLas24HorasYCalculaVariaciones() {
		when(reporteRepository.resumen(INICIO, FIN))
				.thenReturn(new Resumen(2, 0, new BigDecimal("25500")));
		when(reporteRepository.resumen(INICIO.minusDays(1), INICIO))
				.thenReturn(new Resumen(1, 0, new BigDecimal("10000")));
		when(reporteRepository.productosVendidos(INICIO, FIN)).thenReturn(3);
		when(reporteRepository.productosVendidos(INICIO.minusDays(1), INICIO)).thenReturn(1);
		when(reporteRepository.agrupar(INICIO, FIN, "hora")).thenReturn(List.of(
				new FilaAgrupada(8, 1, new BigDecimal("9500")),
				new FilaAgrupada(9, 1, new BigDecimal("16000"))));
		when(reporteRepository.topProductos(eq(INICIO), eq(FIN), anyInt()))
				.thenReturn(List.of(new FilaProducto("Hamburguesa sencilla", 2, new BigDecimal("16000"))));
		when(pedidoRepository.listarEntre(eq(INICIO), eq(FIN), anyInt())).thenReturn(List.of());

		ReporteVentas reporte = reporteService.generar("dia", "2026-11-08");

		assertEquals("dia", reporte.periodo());
		assertEquals("Domingo, 8 de noviembre de 2026", reporte.tituloPeriodo());
		assertEquals(24, reporte.serie().size());
		assertEquals(2, reporte.pedidos());
		assertEquals(new BigDecimal("12750"), reporte.ticketPromedio());
		assertEquals(new BigDecimal("16000"), reporte.maximoSerie());

		ReporteVentas.PuntoSerie ocho = reporte.serie().get(8);
		assertEquals("08", ocho.etiqueta());
		assertEquals(1, ocho.pedidos());
		assertEquals(59, ocho.alturaPct());

		ReporteVentas.PuntoSerie nueve = reporte.serie().get(9);
		assertEquals(100, nueve.alturaPct());

		assertEquals("up", reporte.variacionVentas().tendencia());
		assertEquals(1, reporte.topProductos().size());
		assertEquals(100, reporte.topProductos().get(0).barraPct());
		assertTrue(reporte.isHaySerie());
		assertTrue(reporte.isHayProductos());
	}

	@Test
	void elReporteMensualRellenaTodosLosDiasDelMes() {
		when(reporteRepository.resumen(any(), any())).thenReturn(new Resumen(0, 0, BigDecimal.ZERO));
		when(reporteRepository.productosVendidos(any(), any())).thenReturn(0);
		when(reporteRepository.agrupar(any(), any(), eq("dia"))).thenReturn(List.of());
		when(reporteRepository.topProductos(any(), any(), anyInt())).thenReturn(List.of());
		when(pedidoRepository.listarEntre(any(), any(), anyInt())).thenReturn(List.of());

		ReporteVentas reporte = reporteService.generar("mes", "2026-02-15");

		assertEquals("mes", reporte.periodo());
		assertEquals(28, reporte.serie().size());
		assertEquals("1", reporte.serie().get(0).etiqueta());
		assertTrue(reporte.serie().get(0).etiquetaVisible());
		assertEquals("28", reporte.serie().get(27).etiqueta());
		assertTrue(reporte.serie().get(27).etiquetaVisible());
		assertFalse(reporte.isHaySerie());
	}

	@Test
	void elReporteAnualUsaLosDoceMeses() {
		when(reporteRepository.resumen(any(), any())).thenReturn(new Resumen(0, 0, BigDecimal.ZERO));
		when(reporteRepository.productosVendidos(any(), any())).thenReturn(0);
		when(reporteRepository.agrupar(any(), any(), eq("mes"))).thenReturn(List.of());
		when(reporteRepository.topProductos(any(), any(), anyInt())).thenReturn(List.of());
		when(pedidoRepository.listarEntre(any(), any(), anyInt())).thenReturn(List.of());

		ReporteVentas reporte = reporteService.generar("anio", "2026-11-08");

		assertEquals("anio", reporte.periodo());
		assertEquals(12, reporte.serie().size());
		assertEquals("Ene", reporte.serie().get(0).etiqueta());
		assertEquals("Dic", reporte.serie().get(11).etiqueta());
		assertEquals("Año 2026", reporte.tituloPeriodo());
	}

	@Test
	void unPeriodoInvalidoUsaElDiaActual() {
		when(reporteRepository.resumen(any(), any())).thenReturn(new Resumen(0, 0, BigDecimal.ZERO));
		when(reporteRepository.productosVendidos(any(), any())).thenReturn(0);
		when(reporteRepository.agrupar(any(), any(), eq("hora"))).thenReturn(List.of());
		when(reporteRepository.topProductos(any(), any(), anyInt())).thenReturn(List.of());
		when(pedidoRepository.listarEntre(any(), any(), anyInt())).thenReturn(List.of());

		ReporteVentas reporte = reporteService.generar("semestre", "invalida");

		assertEquals("dia", reporte.periodo());
		assertEquals(LocalDate.now().toString(), reporte.fechaIso());
		assertEquals(24, reporte.serie().size());
	}
}
