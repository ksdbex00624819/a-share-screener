package com.like.a_share_screener.client;

import java.time.Duration;
import java.util.Map;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DefaultEastmoneyHttpInvoker implements EastmoneyHttpInvoker {
	private final RestClient.Builder restClientBuilder;

	public DefaultEastmoneyHttpInvoker(RestClient.Builder restClientBuilder) {
		this.restClientBuilder = restClientBuilder;
	}

	@Override
	public String get(String url, Map<String, String> headers, int timeoutMs) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
		requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
		RestClient client = restClientBuilder.requestFactory(requestFactory).build();
		return client.get()
				.uri(url)
				.headers(httpHeaders -> {
					if (headers != null) {
						headers.forEach(httpHeaders::set);
					}
				})
				.retrieve()
				.body(String.class);
	}
}
