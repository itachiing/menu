package com.restaurante.menu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.restaurante.menu.repository.UsuarioRepository;

@Configuration
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
		return username -> usuarioRepository.buscarPorUsername(username)
				.filter(u -> Boolean.TRUE.equals(u.getActivo()))
				.map(u -> User.withUsername(u.getUsername())
						.password(u.getPassword())
						.roles(u.getRol())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o inactivo: " + username));
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/login", "/registro", "/css/**", "/js/**", "/images/**", "/uploads/**", "/menu", "/api/menu",
						"/api/mesas/codigo/**")
				.permitAll()
				.requestMatchers(HttpMethod.POST, "/api/pedidos", "/api/chatbot/mensaje")
				.permitAll()
				.requestMatchers(HttpMethod.DELETE, "/api/chatbot/historial")
				.permitAll()
				.requestMatchers("/admin/pedidos/**", "/api/pedidos/**")
				.hasAnyRole("ADMIN", "COCINA", "MESERO")
				.requestMatchers("/admin/**")
				.hasRole("ADMIN")
				.requestMatchers("/api/**")
				.hasRole("ADMIN")
				.anyRequest()
				.authenticated())
			// Los endpoints /api/** se pueden probar desde Postman con autenticación básica.
			.httpBasic(Customizer.withDefaults())
			.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
			.formLogin(form -> form
				.loginPage("/login")
				.defaultSuccessUrl("/", true)
				.permitAll())
			.logout(logout -> logout
				.logoutSuccessUrl("/login?logout")
				.permitAll())
			.exceptionHandling(errors -> errors.accessDeniedPage("/acceso-denegado"));

		return http.build();
	}
}
