package com.restaurante.menu.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.restaurante.menu.model.dto.CategoriaMenu;
import com.restaurante.menu.model.dao.AdicionalDAO;
import com.restaurante.menu.model.dao.CategoriaDAO;
import com.restaurante.menu.model.dao.MesaDAO;
import com.restaurante.menu.model.dao.PedidoDAO;
import com.restaurante.menu.model.dao.ProductoDAO;
import com.restaurante.menu.model.dao.UsuarioDAO;
import com.restaurante.menu.model.dao.VarianteProductoDAO;
import com.restaurante.menu.vo.Adicional;
import com.restaurante.menu.vo.Categoria;
import com.restaurante.menu.vo.Mesa;
import com.restaurante.menu.vo.Producto;
import com.restaurante.menu.vo.Usuario;
import com.restaurante.menu.vo.VarianteProducto;

@Controller
public class PanelController {

	private static final List<String> ROLES = List.of("ADMIN", "COCINA", "MESERO");
	private static final List<String> ESTADOS_PEDIDO = List.of(
			"RECIBIDO", "EN_PREPARACION", "LISTO", "ENTREGADO", "CANCELADO");

	private final CategoriaDAO categoriaDAO;
	private final ProductoDAO productoDAO;
	private final VarianteProductoDAO varianteDAO;
	private final AdicionalDAO adicionalDAO;
	private final MesaDAO mesaDAO;
	private final PedidoDAO pedidoDAO;
	private final UsuarioDAO usuarioDAO;
	private final PasswordEncoder passwordEncoder;
	private final String whatsappNumero;
	private final String restauranteNombre;

	public PanelController(CategoriaDAO categoriaDAO, ProductoDAO productoDAO,
			VarianteProductoDAO varianteDAO, AdicionalDAO adicionalDAO, MesaDAO mesaDAO,
			PedidoDAO pedidoDAO, UsuarioDAO usuarioDAO, PasswordEncoder passwordEncoder,
			@Value("${restaurante.whatsapp:573000000000}") String whatsappNumero,
			@Value("${restaurante.nombre:Asados al Carbón}") String restauranteNombre) {
		this.categoriaDAO = categoriaDAO;
		this.productoDAO = productoDAO;
		this.varianteDAO = varianteDAO;
		this.adicionalDAO = adicionalDAO;
		this.mesaDAO = mesaDAO;
		this.pedidoDAO = pedidoDAO;
		this.usuarioDAO = usuarioDAO;
		this.passwordEncoder = passwordEncoder;
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
		model.addAttribute("roles", ROLES);
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
			String uNombre = Validaciones.texto(nombre, "nombre", 100);
			String uUsername = Validaciones.texto(username, "username", 60);

			if (password == null || password.length() < 6) {
				throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
			}
			if (confirmPassword != null && !password.equals(confirmPassword)) {
				throw new IllegalArgumentException("Las contraseñas no coinciden.");
			}
			if (usuarioDAO.buscarPorUsername(uUsername).isPresent()) {
				throw new IllegalArgumentException("El nombre de usuario '" + uUsername + "' ya existe.");
			}

			String rolAsignado = (rol != null && ROLES.contains(rol.trim().toUpperCase(Locale.ROOT)))
					? rol.trim().toUpperCase(Locale.ROOT) : "ADMIN";

			usuarioDAO.insertar(new Usuario(null, uUsername, passwordEncoder.encode(password),
					uNombre, rolAsignado, true));

			redirect.addFlashAttribute("mensaje", "¡Cuenta creada con éxito! Ya puedes iniciar sesión con tu usuario y contraseña.");
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
		var variantesPorProducto = varianteDAO.listarDisponibles().stream()
				.collect(java.util.stream.Collectors.groupingBy(VarianteProducto::getProductoId));
		var productosPorCategoria = productoDAO.listar(null, true).stream()
				.collect(java.util.stream.Collectors.groupingBy(Producto::getCategoriaId));
		List<CategoriaMenu> menu = categoriaDAO.listar(true).stream()
				.map(categoria -> new CategoriaMenu(categoria,
						productosPorCategoria.getOrDefault(categoria.getId(), List.of()).stream()
						.map(producto -> new CategoriaMenu.ProductoMenu(producto,
								variantesPorProducto.getOrDefault(producto.getId(), List.of())))
						.toList()))
				.filter(categoria -> !categoria.productos().isEmpty())
				.toList();

		model.addAttribute("menu", menu);
		model.addAttribute("adicionales", adicionalDAO.listar(null, true));
		model.addAttribute("mesas", mesaDAO.listar().stream().filter(m -> Boolean.TRUE.equals(m.getActiva())).toList());

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
		model.addAttribute("productosCount", productoDAO.listar(null, false).size());
		model.addAttribute("categoriasCount", categoriaDAO.listar(false).size());
		model.addAttribute("mesasCount", mesaDAO.listar().size());
		model.addAttribute("pedidos", pedidoDAO.listarActivos());
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "dashboard";
	}

	@GetMapping("/admin/pedidos")
	public String pedidos(@RequestParam(required = false) String estado, Model model) {
		model.addAttribute("pedidos", estado == null || estado.isBlank()
				? pedidoDAO.listarActivos() : pedidoDAO.listarPorEstado(estado));
		model.addAttribute("estado", estado == null ? "" : estado);
		model.addAttribute("estados", ESTADOS_PEDIDO);
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "pedidos";
	}

	@PostMapping("/admin/pedidos/estado")
	public String cambiarEstadoPedido(@RequestParam Integer id, @RequestParam String estado,
			RedirectAttributes redirect) {
		try {
			pedidoDAO.cambiarEstado(id, estado);
			redirect.addFlashAttribute("mensaje", "El estado del pedido fue actualizado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/pedidos";
	}

	@GetMapping("/admin/productos")
	public String productos(Model model) {
		model.addAttribute("productos", productoDAO.listar(null, false));
		model.addAttribute("categorias", categoriaDAO.listar(false));
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "productos";
	}

	@PostMapping("/admin/productos/guardar")
	public String guardarProducto(Producto producto, RedirectAttributes redirect) {
		try {
			producto.setNombre(Validaciones.texto(producto.getNombre(), "nombre", 100));
			producto.setDescripcion(Validaciones.opcional(producto.getDescripcion(), "descripcion", 300));
			producto.setImagen(Validaciones.opcional(producto.getImagen(), "imagen", 255));
			Validaciones.precio(producto.getPrecio(), "precio");
			if (producto.getId() == null) {
				productoDAO.insertar(producto);
			} else {
				productoDAO.actualizar(producto);
			}
			redirect.addFlashAttribute("mensaje", "Producto guardado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/productos";
	}

	@PostMapping("/admin/productos/eliminar")
	public String eliminarProducto(@RequestParam Integer id, RedirectAttributes redirect) {
		try {
			productoDAO.eliminar(id);
			redirect.addFlashAttribute("mensaje", "Producto eliminado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", "No se pudo eliminar el producto. Puede estar asociado a pedidos.");
		}
		return "redirect:/admin/productos";
	}

	@GetMapping("/admin/categorias")
	public String categorias(Model model) {
		model.addAttribute("categorias", categoriaDAO.listar(false));
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "categorias";
	}

	@PostMapping("/admin/categorias/guardar")
	public String guardarCategoria(Categoria categoria, RedirectAttributes redirect) {
		try {
			categoria.setNombre(Validaciones.texto(categoria.getNombre(), "nombre", 60));
			if (categoria.getId() == null) {
				categoriaDAO.insertar(categoria);
			} else {
				categoriaDAO.actualizar(categoria);
			}
			redirect.addFlashAttribute("mensaje", "Categoría guardada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/categorias";
	}

	@PostMapping("/admin/categorias/eliminar")
	public String eliminarCategoria(@RequestParam Integer id, RedirectAttributes redirect) {
		try {
			categoriaDAO.eliminar(id);
			redirect.addFlashAttribute("mensaje", "Categoría eliminada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", "No se pudo eliminar. Comprueba si tiene productos asociados.");
		}
		return "redirect:/admin/categorias";
	}

	@GetMapping("/admin/mesas")
	public String mesas(Model model) {
		model.addAttribute("mesas", mesaDAO.listar());
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "mesas";
	}

	@PostMapping("/admin/mesas/guardar")
	public String guardarMesa(Mesa mesa, RedirectAttributes redirect) {
		try {
			if (mesa.getNumero() == null || mesa.getNumero() < 1) {
				throw new IllegalArgumentException("El número debe ser mayor que cero.");
			}
			if (mesa.getId() == null) {
				mesaDAO.insertar(mesa);
			} else {
				mesaDAO.actualizar(mesa);
			}
			redirect.addFlashAttribute("mensaje", "Mesa guardada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/mesas";
	}

	@PostMapping("/admin/mesas/eliminar")
	public String eliminarMesa(@RequestParam Integer id, RedirectAttributes redirect) {
		try {
			mesaDAO.eliminar(id);
			redirect.addFlashAttribute("mensaje", "Mesa eliminada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", "No se pudo eliminar. Comprueba si tiene pedidos asociados.");
		}
		return "redirect:/admin/mesas";
	}

	@GetMapping("/admin/adicionales")
	public String adicionales(Model model) {
		model.addAttribute("adicionales", adicionalDAO.listar(null, false));
		model.addAttribute("categorias", categoriaDAO.listar(false));
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "adicionales";
	}

	@PostMapping("/admin/adicionales/guardar")
	public String guardarAdicional(Adicional adicional, RedirectAttributes redirect) {
		try {
			adicional.setNombre(Validaciones.texto(adicional.getNombre(), "nombre", 80));
			if (adicional.getPrecio() == null) {
				throw new IllegalArgumentException("El precio es obligatorio.");
			}
			Validaciones.precio(adicional.getPrecio(), "precio");
			if (adicional.getId() == null) {
				adicionalDAO.insertar(adicional);
			} else {
				adicionalDAO.actualizar(adicional);
			}
			redirect.addFlashAttribute("mensaje", "Adicional guardado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/adicionales";
	}

	@PostMapping("/admin/adicionales/eliminar")
	public String eliminarAdicional(@RequestParam Integer id, RedirectAttributes redirect) {
		try {
			adicionalDAO.eliminar(id);
			redirect.addFlashAttribute("mensaje", "Adicional eliminado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", "No se pudo eliminar. Comprueba si está incluido en pedidos.");
		}
		return "redirect:/admin/adicionales";
	}

	@GetMapping("/admin/variantes")
	public String variantes(Model model) {
		List<Producto> productos = productoDAO.listar(null, false);
		List<VarianteFila> variantes = productos.stream()
				.flatMap(producto -> varianteDAO.listarPorProducto(producto.getId(), false).stream()
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
			variante.setNombre(Validaciones.texto(variante.getNombre(), "nombre", 60));
			Validaciones.precio(variante.getPrecio(), "precio");
			if (variante.getId() == null) {
				varianteDAO.insertar(variante);
			} else {
				varianteDAO.actualizar(variante);
			}
			redirect.addFlashAttribute("mensaje", "Variante guardada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/variantes";
	}

	@PostMapping("/admin/variantes/eliminar")
	public String eliminarVariante(@RequestParam Integer id, RedirectAttributes redirect) {
		try {
			varianteDAO.eliminar(id);
			redirect.addFlashAttribute("mensaje", "Variante eliminada.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", "No se pudo eliminar esta variante.");
		}
		return "redirect:/admin/variantes";
	}

	@GetMapping("/admin/usuarios")
	public String usuarios(Model model) {
		model.addAttribute("usuarios", usuarioDAO.listar());
		model.addAttribute("roles", ROLES);
		model.addAttribute("restauranteNombre", restauranteNombre);
		return "usuarios";
	}

	@PostMapping("/admin/usuarios/guardar")
	public String guardarUsuario(@RequestParam(required = false) Integer id,
			@RequestParam String username, @RequestParam String nombre, @RequestParam String rol,
			@RequestParam(required = false) String password,
			@RequestParam(defaultValue = "false") boolean activo,
			Authentication authentication, RedirectAttributes redirect) {
		try {
			String rolNormalizado = rol.trim().toUpperCase(Locale.ROOT);
			if (!ROLES.contains(rolNormalizado)) {
				throw new IllegalArgumentException("El rol seleccionado no es válido.");
			}
			if (id == null) {
				if (password == null || password.length() < 10) {
					throw new IllegalArgumentException("La contraseña inicial debe tener al menos 10 caracteres.");
				}
				usuarioDAO.insertar(new Usuario(null, username.trim(), passwordEncoder.encode(password),
						nombre.trim(), rolNormalizado, activo));
			} else {
				Usuario existente = usuarioDAO.buscarPorId(id)
						.orElseThrow(() -> new IllegalArgumentException("El usuario ya no existe."));
				if (password != null && !password.isBlank() && password.length() < 10) {
					throw new IllegalArgumentException("La nueva contraseña debe tener al menos 10 caracteres.");
				}
				protegerUltimoAdministrador(existente, rolNormalizado, activo);
				existente.setUsername(username.trim());
				existente.setNombre(nombre.trim());
				existente.setRol(rolNormalizado);
				existente.setActivo(activo);
				usuarioDAO.actualizar(existente);
				if (password != null && !password.isBlank()) {
					usuarioDAO.cambiarPassword(id, passwordEncoder.encode(password));
				}
			}
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
			Usuario usuario = usuarioDAO.buscarPorId(id)
					.orElseThrow(() -> new IllegalArgumentException("El usuario ya no existe."));
			if (usuario.getUsername().equals(authentication.getName())) {
				throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
			}
			protegerUltimoAdministrador(usuario, "", false);
			usuarioDAO.eliminar(id);
			redirect.addFlashAttribute("mensaje", "Usuario eliminado.");
		} catch (RuntimeException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/usuarios";
	}

	private void protegerUltimoAdministrador(Usuario usuario, String nuevoRol, boolean nuevoActivo) {
		if (!"ADMIN".equalsIgnoreCase(usuario.getRol()) || !Boolean.TRUE.equals(usuario.getActivo())
				|| ("ADMIN".equals(nuevoRol) && nuevoActivo)) {
			return;
		}
		long administradores = usuarioDAO.listar().stream()
				.filter(otro -> !otro.getId().equals(usuario.getId()))
				.filter(otro -> "ADMIN".equalsIgnoreCase(otro.getRol()))
				.filter(otro -> Boolean.TRUE.equals(otro.getActivo()))
				.count();
		if (administradores == 0) {
			throw new IllegalArgumentException("Debe permanecer al menos un administrador activo.");
		}
	}

	public record VarianteFila(VarianteProducto variante, String productoNombre) {
	}
}
