package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

@ContextConfiguration(classes = DemoApplication.class)
@ActiveProfiles("test")
@SpringBootTest
public class RestAPITest {

	@Autowired
	protected WebClient webClient;	

	@Test
	public void testPublicUrl() throws URISyntaxException {
		final String result = webClient.get().uri(new URI("https://www.google.com/")).retrieve().bodyToMono(String.class).block();
		assertThat(result).isNotNull();
	}
}
