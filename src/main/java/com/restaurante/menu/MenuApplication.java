package com.restaurante.menu;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MenuApplication {

	public static void main(String[] args) {
		cargarArchivoEnv();
		SpringApplication.run(MenuApplication.class, args);
	}

	/**
	 * Carga automáticamente variables de entorno desde un archivo local .env si existe.
	 */
	private static void cargarArchivoEnv() {
		Path path = Path.of(".env");
		if (!Files.exists(path)) {
			path = Path.of(System.getProperty("user.dir"), ".env");
		}
		if (Files.exists(path)) {
			try {
				List<String> lineas = Files.readAllLines(path);
				for (String linea : lineas) {
					String trimmed = linea.trim();
					if (trimmed.isEmpty() || trimmed.startsWith("#")) {
						continue;
					}
					int sep = trimmed.indexOf('=');
					if (sep > 0) {
						String key = trimmed.substring(0, sep).trim();
						String val = trimmed.substring(sep + 1).trim();
						if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
							val = val.substring(1, val.length() - 1);
						}
						if (!val.isEmpty()) {
							System.setProperty(key, val);
						}
					}
				}
			} catch (Exception ignored) {
			}
		}
	}
}
