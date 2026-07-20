package com.narangnorang.config;

import com.narangnorang.jwt.JwtAuthenticationFilter;
import com.narangnorang.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtUtil jwtUtil;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// LoginServiceImpl 에서 DI 사용
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
		return authenticationConfiguration.getAuthenticationManager();
	}

	// csrf 활성화
	// 회원 가입 화면 처리, 가입 처리 permitAll()
	@Bean
	SecurityFilterChain filterChain(
			HttpSecurity http,
			com.narangnorang.config.MyAuthenticationEntryPoint entryPoint
	) throws Exception{
		return http
				// basicLogin, formLogin 사용 X, csrf X, session X
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement( session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests( request -> request
						.requestMatchers(
								"/users/register"
						).permitAll()
						.anyRequest().authenticated()
				)
				.exceptionHandling( exceptionHandling -> exceptionHandling.authenticationEntryPoint(entryPoint))
				.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
				.build();
	}
}
