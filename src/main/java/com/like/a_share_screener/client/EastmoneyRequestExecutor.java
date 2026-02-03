package com.like.a_share_screener.client;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class EastmoneyRequestExecutor {
	private final EastmoneyHttpInvoker httpInvoker;
	private final EastmoneyRequestProperties requestProperties;
	private final EastmoneyProperties properties;
	private final EastmoneyRequestPacer pacer;

	public EastmoneyRequestExecutor(EastmoneyHttpInvoker httpInvoker, EastmoneyRequestProperties requestProperties,
			EastmoneyProperties properties, EastmoneyRequestPacer pacer) {
		this.httpInvoker = httpInvoker;
		this.requestProperties = requestProperties;
		this.properties = properties;
		this.pacer = pacer;
	}

	public String get(String url, String context) {
		pacer.pace();
		return httpInvoker.get(url, buildHeaders(), requestProperties.getTimeoutMs());
	}

	private Map<String, String> buildHeaders() {
		return Map.of(
				HttpHeaders.USER_AGENT, properties.getUserAgent(),
				HttpHeaders.REFERER, properties.getReferer()
		);
	}
}
