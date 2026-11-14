package com.restaurante.menu.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Expone las fotos subidas por el panel en la ruta pública /uploads/**.
 * Los archivos se guardan fuera del classpath para que no se pierdan al recompilar.
 */
@Configuration
public class UploadsConfig implements WebMvcConfigurer {

	private final Path directorio;

	public UploadsConfig(@Value("${menu.uploads-dir:uploads}") String directorio) {
		this.directorio = Paths.get(directorio).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.directorio);
		} catch (IOException exception) {
			throw new IllegalStateException(
					"No se pudo crear la carpeta de imágenes: " + this.directorio, exception);
		}
	}

	public Path getDirectorio() {
		return directorio;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String ubicacion = directorio.toUri().toString();
		if (!ubicacion.endsWith("/")) {
			ubicacion += "/";
		}
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(ubicacion)
				.setCachePeriod(3600);
	}
}
