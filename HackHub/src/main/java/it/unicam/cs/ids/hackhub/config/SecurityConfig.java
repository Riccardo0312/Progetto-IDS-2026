package it.unicam.cs.ids.hackhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain developmentSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
				.csrf(csrfConfigurer -> csrfConfigurer.disable())
				.headers(headersConfigurer ->
						headersConfigurer.frameOptions(frameOptionsConfigurer -> frameOptionsConfigurer.sameOrigin()))
				.formLogin(formLoginConfigurer -> formLoginConfigurer.disable())
				.httpBasic(httpBasicConfigurer -> httpBasicConfigurer.disable())
				.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
						authorizationManagerRequestMatcherRegistry.anyRequest().permitAll())
				.build();
	}

}
