package com.restaurante.menu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.restaurante.menu.model.dao.UsuarioDAO;

@Configuration
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(UsuarioDAO usuarioDAO) {
		return username -> usuarioDAO.buscarPorUsername(username)
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
				.requestMatchers("/", "/login", "/registro", "/css/**", "/js/**", "/images/**", "/menu", "/api/menu",
						"/api/mesas/codigo/**")
				.permitAll()
				.requestMatchers(HttpMethod.POST, "/api/pedidos")
				.permitAll()
				.requestMatchers("/admin/pedidos/**", "/api/pedidos/**")
				.hasAnyRole("ADMIN", "COCINA", "MESERO")
				.requestMatchers("/admin/**")
				.hasRole("ADMIN")
				.requestMatchers("/api/**")
				.hasRole("ADMIN")
				.anyRequest()
				.authenticated())
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
