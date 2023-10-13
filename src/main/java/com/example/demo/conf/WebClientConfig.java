package com.example.demo.conf;

import java.net.InetSocketAddress;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ClientCredentialsReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.CustomResolverGroup;

import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider.Proxy;

@Configuration
public class WebClientConfig {

	@Bean
	HttpClient httpClient(final ReactorResourceFactory resourceFactory) {
		return HttpClient.create().resolver(CustomResolverGroup.INSTANCE).proxy(proxy -> proxy.type(Proxy.HTTP)
				.address(new InetSocketAddress("127.0.0.1", 3128)).nonProxyHosts("localhost"));
	}

	@Bean
	WebClient webClient(ReactiveClientRegistrationRepository registrationRepository,
			ReactiveOAuth2AuthorizedClientService clientService, HttpClient httpClient) {
		AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager clientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
				registrationRepository, clientService);

		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
				clientManager);
		oauth.setDefaultClientRegistrationId("testprovider");
		return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).filter(oauth).build();
	}

	@Bean
	ReactiveOAuth2AuthorizedClientService clientService(ReactiveClientRegistrationRepository registrationRepository) {
		return new InMemoryReactiveOAuth2AuthorizedClientService(registrationRepository);
	}



	@Bean
	ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
			ReactiveClientRegistrationRepository clientRegistrationRepository,
			ReactiveOAuth2AuthorizedClientService authorizedClientService, HttpClient httpClient) {

		return configureHttpProxy(new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
				clientRegistrationRepository, authorizedClientService), httpClient);
	}

	private AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager configureHttpProxy(
			AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager,
			HttpClient httpClient) {
		// set the webclient with proxy configuration in the
		// ReactiveOAuth2AccessTokenResponseClient
		WebClientReactiveClientCredentialsTokenResponseClient tokenResponseClient = new WebClientReactiveClientCredentialsTokenResponseClient();
		tokenResponseClient
				.setWebClient(WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build());

		// set the ReactiveOAuth2AccessTokenResponseClient with webclient configuration
		// in the ReactiveOAuth2AuthorizedClientProvider
		ClientCredentialsReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = new ClientCredentialsReactiveOAuth2AuthorizedClientProvider();
		authorizedClientProvider.setAccessTokenResponseClient(tokenResponseClient);

		// set the ReactiveOAuth2AuthorizedClientProvider in the
		// ReactiveOAuth2AuthorizedClientManager
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

		return authorizedClientManager;
	}
}
