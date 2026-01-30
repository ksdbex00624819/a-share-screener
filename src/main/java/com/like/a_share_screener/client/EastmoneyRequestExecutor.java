package com.like.a_share_screener.client;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class EastmoneyRequestExecutor {
	private static final Logger log = LoggerFactory.getLogger(EastmoneyRequestExecutor.class);
	private final RestClient restClient;
	private final EastmoneyProperties properties;
	private final AtomicLong lastRequestTime = new AtomicLong(0);
	private final Object throttleLock = new Object();

	public EastmoneyRequestExecutor(EastmoneyProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.builder()
				.defaultHeader(HttpHeaders.USER_AGENT, properties.getUserAgent())
				.defaultHeader(HttpHeaders.REFERER, properties.getReferer())
				.build();
	}

	public String get(String url, String context) {
		int maxRetries = properties.getRequest().getMaxRetries();
		int attempt = 0;
		while (true) {
			throttle();
			try {
				return restClient.get().uri(url).retrieve().body(String.class);
			} catch (RestClientResponseException ex) {
				if (ex.getStatusCode().is5xxServerError() && attempt < maxRetries) {
					attempt++;
					log.warn("Eastmoney request retrying (attempt {}/{}), context={}, status={}",
							attempt, maxRetries, context, ex.getStatusCode());
					sleepBackoff(attempt);
					continue;
				}
				throw ex;
			} catch (RestClientException ex) {
				if (attempt < maxRetries) {
					attempt++;
					log.warn("Eastmoney request retrying (attempt {}/{}), context={}, error={}",
							attempt, maxRetries, context, ex.getClass().getSimpleName());
					sleepBackoff(attempt);
					continue;
				}
				throw ex;
			}
		}
	}

	public void sleepBackoff(int attempt) {
		long baseBackoffMs = properties.getRequest().getBackoffMs();
		long exponential = baseBackoffMs * (1L << Math.max(attempt - 1, 0));
		long jitter = ThreadLocalRandom.current().nextLong(0, baseBackoffMs + 1);
		sleep(Duration.ofMillis(exponential + jitter));
	}

	private void throttle() {
		long minIntervalMs = properties.getRequest().getMinIntervalMs();
		if (minIntervalMs <= 0) {
			return;
		}
		synchronized (throttleLock) {
			long now = System.currentTimeMillis();
			long elapsed = now - lastRequestTime.get();
			if (elapsed < minIntervalMs) {
				sleep(Duration.ofMillis(minIntervalMs - elapsed));
			}
			lastRequestTime.set(System.currentTimeMillis());
		}
	}

	private void sleep(Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while throttling Eastmoney request", ex);
		}
	}
}
