package com.example.demo.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

//import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

	@Bean
	SecurityWebFilterChain filterChain(ServerHttpSecurity  httpSecurity) throws Exception {
		httpSecurity
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.disable())
//			.authorizeHttpRequests(authorize -> authorize.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR)
//								.permitAll().requestMatchers(AnyRequestMatcher.INSTANCE).permitAll())
			.oauth2Client(Customizer.withDefaults());
		return httpSecurity.build();
	}	
	

}
