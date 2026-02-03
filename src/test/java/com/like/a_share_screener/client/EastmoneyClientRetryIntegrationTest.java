package com.like.a_share_screener.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.like.a_share_screener.domain.Candle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EastmoneyClientRetryIntegrationTest {

	@Test
	void retriesOnTransientRcThenSucceeds() {
		EastmoneyRequestProperties requestProperties = new EastmoneyRequestProperties();
		requestProperties.setMaxRetries(2);
		requestProperties.setBackoffBaseMs(1);
		requestProperties.setBackoffMaxMs(5);
		requestProperties.setJitterMs(0);
		requestProperties.setRetryRcCodes(List.of(102));
		requestProperties.setMinIntervalMs(0);
		FakeSleeper sleeper = new FakeSleeper();
		EastmoneyRequestPacer pacer = new EastmoneyRequestPacer(requestProperties, sleeper);

		EastmoneyProperties properties = new EastmoneyProperties();
		properties.setBaseUrl("https://example.com");
		properties.setFields1("f1");
		properties.setFields2("f2");
		properties.setUt("ut");
		properties.setUserAgent("ua");
		properties.setReferer("ref");

		List<String> responses = List.of(
				"{\"rc\":102,\"data\":null}",
				"{\"rc\":102,\"data\":null}",
				"{\"rc\":0,\"data\":{\"klines\":[\"2024-01-02,12.34,12.50,12.60,12.20,123456,987654.32,3.21,1.29,0.16,0.45\"]}}"
		);
		FakeHttpInvoker invoker = new FakeHttpInvoker(responses);
		EastmoneyRequestExecutor requestExecutor = new EastmoneyRequestExecutor(invoker, requestProperties, properties,
				pacer);
		EastmoneyRetryExecutor retryExecutor = new EastmoneyRetryExecutor(requestProperties, sleeper);
		EastmoneyKlineClient client = new EastmoneyKlineClient(requestExecutor, properties, requestProperties,
				retryExecutor, new EastmoneyKlineParser(new ObjectMapper()));

		List<Candle> candles = client.fetchKlines("000001", "1.000001", "1d", 101, 1, "0", "20240102", null);

		Assertions.assertThat(candles).hasSize(1);
		Assertions.assertThat(invoker.callCount()).isEqualTo(3);
	}

	private static class FakeSleeper implements Sleeper {
		@Override
		public void sleepMs(long ms) {
			// no-op
		}
	}

	private static class FakeHttpInvoker implements EastmoneyHttpInvoker {
		private final List<String> responses;
		private final AtomicInteger calls = new AtomicInteger();

		private FakeHttpInvoker(List<String> responses) {
			this.responses = new ArrayList<>(responses);
		}

		@Override
		public String get(String url, Map<String, String> headers, int timeoutMs) {
			int index = calls.getAndIncrement();
			return responses.get(index);
		}

		private int callCount() {
			return calls.get();
		}
	}
}
