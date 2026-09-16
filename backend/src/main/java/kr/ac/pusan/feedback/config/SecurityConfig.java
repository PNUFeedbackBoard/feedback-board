package kr.ac.pusan.feedback.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, exception) ->
								response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
						.accessDeniedHandler((request, response, exception) ->
								response.sendError(HttpServletResponse.SC_FORBIDDEN)))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**",
								"/h2-console/**",
								"/api/dev/**"
						).permitAll()
						.requestMatchers("/api/feedbacks/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/admin/**")
						.hasAnyRole("DEVELOPER", "VIEWER")
						.requestMatchers("/api/admin/**").hasRole("DEVELOPER")
						.requestMatchers("/api/auth/**").authenticated()
						.anyRequest().permitAll());

		return http.build();
	}
}
