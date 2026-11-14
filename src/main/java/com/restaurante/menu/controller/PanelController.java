package com.restaurante.menu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.restaurante.menu.dto.CategoriaMenu;
import com.restaurante.menu.dto.ReporteVentas;
import com.restaurante.menu.entity.Adicional;
import com.restaurante.menu.entity.Categoria;
import com.restaurante.menu.entity.Mesa;
import com.restaurante.menu.entity.Producto;
import com.restaurante.menu.entity.VarianteProducto;
import com.restaurante.menu.service.AdicionalService;
import com.restaurante.menu.service.CategoriaService;
import com.restaurante.menu.service.ImagenProductoService;
import com.restaurante.menu.service.MenuService;
import com.restaurante.menu.service.MesaService;
import com.restaurante.menu.service.PedidoService;
import com.restaurante.menu.service.ProductoService;
import com.restaurante.menu.service.ReporteService;
import com.restaurante.menu.service.UsuarioService;
import com.restaurante.menu.service.VarianteProductoService;

/**
 * Controlador de las vistas Thymeleaf del menú público y del panel administrativo.
 * Toda la lógica de negocio se delega en la capa de servicios.
 */
@Controller
public class PanelController {

	private static final List<String> ESTADOS_PEDIDO = List.of(
			"RECIBIDO", "EN_PREPARACION", "LISTO", "ENTREGADO", "CANCELADO");

	private final CategoriaService categoriaService;
	private final ProductoService productoService;
	private final ImagenProductoService imagenProductoService;
	private final VarianteProductoService varianteService;
	private final AdicionalService adicionalService;
	private final MesaService mesaService;
	private final PedidoService pedidoService;
	private final ReporteService reporteService;
	private final UsuarioService usuarioService;
	private final MenuService menuService;
	private final String whatsappNumero;
	private final String restauranteNombre;

	public PanelController(CategoriaService categoriaService, ProductoService productoService,
			ImagenProductoService imagenProductoService,
			VarianteProductoService varianteService, AdicionalService adicionalService,
			MesaService mesaService, PedidoService pedidoService, UsuarioService usuarioService,
			ReporteService reporteService,
			MenuService menuService,
			@Value("${restaurante.whatsapp:573000000000}") String whatsappNumero,
			@Value("${restaurante.nombre:Asados al Carbón}") String restauranteNombre) {
		this.categoriaService = categoriaService;
		this.productoService = productoService;
		this.imagenProductoService = imagenProductoService;
		this.varianteService = varianteService;
		this.adicionalService = adicionalService;
		this.mesaService = mesaService;
		this.pedidoService = pedidoService;
		this.reporteService = reporteService;
		this.usuarioService = usuarioService;
		this.menuService = menuService;
		this.whatsappNumero = whatsappNumero;
		this.restauranteNombre = restauranteNombre;
	}

	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "login";
	}

	@GetMapping("/registro")
	public String registro(Model model) {
		model.addAttribute("restauranteNombre", restauranteNombre);
		model.addAttribute("roles", UsuarioService.ROLES);
		return "registro";
	}

	@PostMapping("/registro")
	public String registrarUsuario(@RequestParam String nombre,
			@RequestParam String username,
			@RequestParam String password,
			@RequestParam(required = false) String confirmPassword,
			@RequestParam(required = false, defaultValue = "ADMIN") String rol,
			RedirectAttributes redirect) {
		try {
			usuarioService.registrar(nombre, username, password, confirmPassword, rol);
			redirect.addFlashAttribute("mensaje",
					"¡Cuenta creada con éxito! Ya puedes iniciar sesión con tu usuario y contraseña.");
			return "redirect:/login";
		} catch (RuntimeException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
			return "redirect:/registro";
		}
	}

	@GetMapping("/acceso-denegado")
	public String accesoDenegado() {
		return "acceso-denegado";
	}

	@GetMapping("/menu")
	public String menu(@RequestParam(required = false) String mesa,
			@RequestParam(required = false) String codigo,
			Model model) {
		List<CategoriaMenu> menu = menuService.obtenerMenu();

		model.addAttribute("menu", menu);
		model.addAttribute("adicionales", adicionalService.listar(null, true));
		model.addAttribute("mesas", mesaService.listar().stream()
				.filter(m -> Boolean.TRUE.equals(m.getActiva())).toList());

		String mesaSel = (codigo != null && !codigo.isBlank()) ? codigo.trim() : (mesa != null ? mesa.trim() : "");
		model.addAttribute("mesaPreseleccionada", mesaSel);
		model.addAttribute("whatsappNumero", whatsappNumero);
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "menu";
	}

	@GetMapping("/")
	public String inicio(Authentication authentication, Model model) {
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/menu";
		}
		model.addAttribute("productosCount", productoService.listar(null, false).size());
		model.addAttribute("categoriasCount", categoriaService.listar(false).size());
		model.addAttribute("mesasCount", mesaService.listar().size());
		model.addAttribute("pedidos", pedidoService.listarActivos());
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "dashboard";
	}

	@GetMapping("/admin/pedidos")
	public String pedidos(@RequestParam(required = false) String estado, Model model) {
		model.addAttribute("pedidos", estado == null || estado.isBlank()
				? pedidoService.listarActivos() : pedidoService.listarPorEstado(estado));
		model.addAttribute("estado", estado == null ? "" : estado);
		model.addAttribute("estados", ESTADOS_PEDIDO);
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "pedidos";
	}

	@PostMapping("/admin/pedidos/estado")
	public String cambiarEstadoPedido(@RequestParam Integer id, @RequestParam String estado,
			RedirectAttributes redirect) {
		try {
			pedidoService.cambiarEstado(id, estado);
			redirect.addFlashAttribute("mensaje", "El estado del pedido fue actualizado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/pedidos";
	}

	@GetMapping("/admin/reportes")
	public String reportes(@RequestParam(required = false) String periodo,
			@RequestParam(required = false) String fecha,
			Model model) {
		ReporteVentas reporte = reporteService.generar(periodo, fecha);
		model.addAttribute("reporte", reporte);
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "reportes";
	}

	@GetMapping("/admin/productos")
	public String productos(Model model) {
		model.addAttribute("productos", productoService.listar(null, false));
		model.addAttribute("categorias", categoriaService.listar(false));
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "productos";
	}

	@PostMapping("/admin/productos/guardar")
	public String guardarProducto(Producto producto,
			@RequestParam(value = "archivoImagen", required = false) MultipartFile archivoImagen,
			RedirectAttributes redirect) {
		String nuevaImagen = null;
		try {
			String imagenAnterior = producto.getId() == null
					? null : productoService.obtener(producto.getId()).getImagen();

			nuevaImagen = imagenProductoService.guardar(archivoImagen);
			if (nuevaImagen != null) {
				producto.setImagen(nuevaImagen);
			}

			Producto guardado = productoService.guardar(producto);

			boolean imagenCambiada = nuevaImagen != null
					|| producto.getImagen() == null || producto.getImagen().isBlank();
			if (imagenCambiada && imagenAnterior != null
					&& !imagenAnterior.equals(guardado.getImagen())) {
				imagenProductoService.eliminar(imagenAnterior);
			}

			redirect.addFlashAttribute("mensaje", "Producto guardado.");
		} catch (RuntimeException exception) {
			if (nuevaImagen != null) {
				imagenProductoService.eliminar(nuevaImagen);
			}
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/productos";
	}

	@PostMapping("/admin/productos/eliminar")
	public String eliminarProducto(@RequestParam Integer id, RedirectAttributes redirect) {
		try {
			Producto producto = productoService.obtener(id);
			productoService.eliminar(id);
			imagenProductoService.eliminar(producto.getImagen());
			redirect.addFlashAttribute("mensaje", "Producto eliminado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", "No se pudo eliminar el producto. Puede estar asociado a pedidos.");
		}
		return "redirect:/admin/productos";
	}

	@GetMapping("/admin/categorias")
	public String categorias(Model model) {
		model.addAttribute("categorias", categoriaService.listar(false));
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "categorias";
	}

	@PostMapping("/admin/categorias/guardar")
	public String guardarCategoria(Categoria categoria, RedirectAttributes redirect) {
		try {
			categoriaService.guardar(categoria);
			redirect.addFlashAttribute("mensaje", "Categoría guardada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/categorias";
	}

	@PostMapping("/admin/categorias/eliminar")
	public String eliminarCategoria(@RequestParam Integer id, RedirectAttributes redirect) {
		try {
			categoriaService.eliminar(id);
			redirect.addFlashAttribute("mensaje", "Categoría eliminada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", "No se pudo eliminar. Comprueba si tiene productos asociados.");
		}
		return "redirect:/admin/categorias";
	}

	@GetMapping("/admin/mesas")
	public String mesas(Model model) {
		model.addAttribute("mesas", mesaService.listar());
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "mesas";
	}

	@PostMapping("/admin/mesas/guardar")
	public String guardarMesa(Mesa mesa, RedirectAttributes redirect) {
		try {
			mesaService.guardar(mesa);
			redirect.addFlashAttribute("mensaje", "Mesa guardada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/mesas";
	}

	@PostMapping("/admin/mesas/eliminar")
	public String eliminarMesa(@RequestParam Integer id, RedirectAttributes redirect) {
		try {
			mesaService.eliminar(id);
			redirect.addFlashAttribute("mensaje", "Mesa eliminada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", "No se pudo eliminar. Comprueba si tiene pedidos asociados.");
		}
		return "redirect:/admin/mesas";
	}

	@GetMapping("/admin/adicionales")
	public String adicionales(Model model) {
		model.addAttribute("adicionales", adicionalService.listar(null, false));
		model.addAttribute("categorias", categoriaService.listar(false));
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "adicionales";
	}

	@PostMapping("/admin/adicionales/guardar")
	public String guardarAdicional(Adicional adicional, RedirectAttributes redirect) {
		try {
			adicionalService.guardar(adicional);
			redirect.addFlashAttribute("mensaje", "Adicional guardado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/adicionales";
	}

	@PostMapping("/admin/adicionales/eliminar")
	public String eliminarAdicional(@RequestParam Integer id, RedirectAttributes redirect) {
		try {
			adicionalService.eliminar(id);
			redirect.addFlashAttribute("mensaje", "Adicional eliminado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", "No se pudo eliminar. Comprueba si está incluido en pedidos.");
		}
		return "redirect:/admin/adicionales";
	}

	@GetMapping("/admin/variantes")
	public String variantes(Model model) {
		List<Producto> productos = productoService.listar(null, false);
		List<VarianteFila> variantes = productos.stream()
				.flatMap(producto -> varianteService.listarPorProducto(producto.getId(), false).stream()
						.map(variante -> new VarianteFila(variante, producto.getNombre())))
				.toList();
		model.addAttribute("variantes", variantes);
		model.addAttribute("productos", productos);
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "variantes";
	}

	@PostMapping("/admin/variantes/guardar")
	public String guardarVariante(VarianteProducto variante, RedirectAttributes redirect) {
		try {
			varianteService.guardar(variante);
			redirect.addFlashAttribute("mensaje", "Variante guardada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/variantes";
	}

	@PostMapping("/admin/variantes/eliminar")
	public String eliminarVariante(@RequestParam Integer id, RedirectAttributes redirect) {
		try {
			varianteService.eliminar(id);
			redirect.addFlashAttribute("mensaje", "Variante eliminada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", "No se pudo eliminar esta variante.");
		}
		return "redirect:/admin/variantes";
	}

	@GetMapping("/admin/usuarios")
	public String usuarios(Model model) {
		model.addAttribute("usuarios", usuarioService.listar());
		model.addAttribute("roles", UsuarioService.ROLES);
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "usuarios";
	}

	@PostMapping("/admin/usuarios/guardar")
	public String guardarUsuario(@RequestParam(required = false) Integer id,
			@RequestParam String username, @RequestParam String nombre, @RequestParam String rol,
			@RequestParam(required = false) String password,
			@RequestParam(defaultValue = "false") boolean activo,
			RedirectAttributes redirect) {
		try {
			usuarioService.guardar(id, username, nombre, rol, password, activo);
			redirect.addFlashAttribute("mensaje", "Usuario guardado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/usuarios";
	}

	@PostMapping("/admin/usuarios/eliminar")
	public String eliminarUsuario(@RequestParam Integer id, Authentication authentication,
			RedirectAttributes redirect) {
		try {
			usuarioService.eliminar(id, authentication.getName());
			redirect.addFlashAttribute("mensaje", "Usuario eliminado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/usuarios";
	}

	public record VarianteFila(VarianteProducto variante, String productoNombre) {
	}
}
