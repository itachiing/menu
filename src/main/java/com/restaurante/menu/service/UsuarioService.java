package com.restaurante.menu.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.restaurante.menu.entity.Usuario;
import com.restaurante.menu.repository.UsuarioRepository;

/**
 * Reglas de negocio de los usuarios del panel administrativo.
 */
@Service
public class UsuarioService {

	public static final List<String> ROLES = List.of("ADMIN", "COCINA", "MESERO");

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public List<Usuario> listar() {
		return usuarioRepository.listar();
	}

	public Usuario obtener(Integer id) {
		return Validaciones.encontrado(usuarioRepository.buscarPorId(id), "Usuario");
	}

	public Optional<Usuario> buscarPorUsername(String username) {
		return usuarioRepository.buscarPorUsername(username);
	}

	/** Autoregistro desde la pantalla pública. */
	public Usuario registrar(String nombre, String username, String password, String confirmPassword, String rol) {
		String nombreLimpio = Validaciones.texto(nombre, "nombre", 100);
		String usernameLimpio = Validaciones.texto(username, "username", 60);

		if (password == null || password.length() < 6) {
			throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
		}
		if (confirmPassword != null && !password.equals(confirmPassword)) {
			throw new IllegalArgumentException("Las contraseñas no coinciden.");
		}
		if (usuarioRepository.buscarPorUsername(usernameLimpio).isPresent()) {
			throw new IllegalArgumentException("El nombre de usuario '" + usernameLimpio + "' ya existe.");
		}

		String rolAsignado = normalizarRol(rol, "ADMIN");
		return usuarioRepository.insertar(new Usuario(null, usernameLimpio,
				passwordEncoder.encode(password), nombreLimpio, rolAsignado, true));
	}

	/** Crea o actualiza un usuario desde el panel de administración. */
	public Usuario guardar(Integer id, String username, String nombre, String rol, String password,
			boolean activo) {
		String rolNormalizado = normalizarRol(rol, null);
		String usernameLimpio = Validaciones.texto(username, "username", 60);
		String nombreLimpio = Validaciones.texto(nombre, "nombre", 100);

		if (id == null) {
			if (password == null || password.length() < 10) {
				throw new IllegalArgumentException("La contraseña inicial debe tener al menos 10 caracteres.");
			}
			return usuarioRepository.insertar(new Usuario(null, usernameLimpio,
					passwordEncoder.encode(password), nombreLimpio, rolNormalizado, activo));
		}

		Usuario existente = obtener(id);
		if (password != null && !password.isBlank() && password.length() < 10) {
			throw new IllegalArgumentException("La nueva contraseña debe tener al menos 10 caracteres.");
		}
		protegerUltimoAdministrador(existente, rolNormalizado, activo);

		existente.setUsername(usernameLimpio);
		existente.setNombre(nombreLimpio);
		existente.setRol(rolNormalizado);
		existente.setActivo(activo);
		usuarioRepository.actualizar(existente);
		if (password != null && !password.isBlank()) {
			usuarioRepository.cambiarPassword(id, passwordEncoder.encode(password));
		}
		return existente;
	}

	/** Crea el administrador inicial a partir de las credenciales de configuración. */
	public Usuario crearInicial(String username, String password, String nombre, String rol) {
		return usuarioRepository.insertar(new Usuario(null, username,
				passwordEncoder.encode(password), nombre, rol, true));
	}

	/** Restablece la contraseña de un usuario (usado por el arranque de la aplicación). */
	public void restablecerPassword(Integer id, String password) {
		obtener(id);
		usuarioRepository.cambiarPassword(id, passwordEncoder.encode(password));
	}

	public void eliminar(Integer id, String usernameActual) {
		Usuario usuario = obtener(id);
		if (usuario.getUsername().equals(usernameActual)) {
			throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
		}
		protegerUltimoAdministrador(usuario, "", false);
		usuarioRepository.eliminar(id);
	}

	private String normalizarRol(String rol, String defecto) {
		if (rol == null || rol.isBlank()) {
			if (defecto != null) {
				return defecto;
			}
			throw new IllegalArgumentException("El rol es obligatorio.");
		}
		String normalizado = rol.trim().toUpperCase(Locale.ROOT);
		if (!ROLES.contains(normalizado)) {
			throw new IllegalArgumentException("El rol seleccionado no es válido.");
		}
		return normalizado;
	}

	private void protegerUltimoAdministrador(Usuario usuario, String nuevoRol, boolean nuevoActivo) {
		if (!"ADMIN".equalsIgnoreCase(usuario.getRol()) || !Boolean.TRUE.equals(usuario.getActivo())
				|| ("ADMIN".equals(nuevoRol) && nuevoActivo)) {
			return;
		}
		long administradores = usuarioRepository.listar().stream()
				.filter(otro -> !otro.getId().equals(usuario.getId()))
				.filter(otro -> "ADMIN".equalsIgnoreCase(otro.getRol()))
				.filter(otro -> Boolean.TRUE.equals(otro.getActivo()))
				.count();
		if (administradores == 0) {
			throw new IllegalArgumentException("Debe permanecer al menos un administrador activo.");
		}
	}
}
