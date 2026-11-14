package com.restaurante.menu.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.restaurante.menu.config.UploadsConfig;

/**
 * Guarda y elimina las fotos de los productos en la carpeta configurada
 * por {@link UploadsConfig}. La base de datos solo almacena la ruta pública.
 */
@Service
public class ImagenProductoService {

	private static final String RUTA_PUBLICA = "/uploads/productos/";
	private static final long TAMANO_MAXIMO = 5L * 1024 * 1024;

	private static final Map<String, String> EXTENSIONES = Map.of(
			"image/jpeg", "jpg",
			"image/png", "png",
			"image/webp", "webp",
			"image/gif", "gif",
			"image/avif", "avif");

	private final Path directorio;

	public ImagenProductoService(UploadsConfig uploadsConfig) {
		this.directorio = uploadsConfig.getDirectorio().resolve("productos").normalize();
	}

	/**
	 * Guarda la imagen recibida y devuelve su ruta pública, o null si no se envió archivo.
	 */
	public String guardar(MultipartFile archivo) {
		if (archivo == null || archivo.isEmpty()) {
			return null;
		}
		if (archivo.getSize() > TAMANO_MAXIMO) {
			throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
		}

		String tipo = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT);
		String extension = EXTENSIONES.get(tipo);
		if (extension == null) {
			throw new IllegalArgumentException("Formato no permitido. Usa JPG, PNG, WEBP, GIF o AVIF");
		}

		try {
			Files.createDirectories(directorio);
			String nombre = UUID.randomUUID() + "." + extension;
			Path destino = directorio.resolve(nombre).normalize();
			if (!destino.startsWith(directorio)) {
				throw new IllegalArgumentException("Ruta de imagen no válida");
			}
			try (InputStream entrada = archivo.getInputStream()) {
				Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
			}
			return RUTA_PUBLICA + nombre;
		} catch (IOException exception) {
			throw new IllegalStateException("No se pudo guardar la imagen del producto", exception);
		}
	}

	/**
	 * Borra un archivo previamente guardado. Ignora rutas externas o inexistentes.
	 */
	public void eliminar(String ruta) {
		if (ruta == null || !ruta.startsWith(RUTA_PUBLICA)) {
			return;
		}
		String nombre = ruta.substring(RUTA_PUBLICA.length());
		if (nombre.isBlank() || nombre.contains("/") || nombre.contains("\\")) {
			return;
		}
		try {
			Path archivo = directorio.resolve(nombre).normalize();
			if (archivo.startsWith(directorio)) {
				Files.deleteIfExists(archivo);
			}
		} catch (IOException ignored) {
			// Si no se puede borrar, la imagen nueva ya quedó guardada y la ficha sigue funcionando.
		}
	}
}
