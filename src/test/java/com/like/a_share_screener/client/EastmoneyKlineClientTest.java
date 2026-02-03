package com.like.a_share_screener.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EastmoneyKlineClientTest {

	@Test
	void throwsWhenRcIsNonZero() {
		EastmoneyRequestExecutor executor = Mockito.mock(EastmoneyRequestExecutor.class);
		EastmoneyProperties properties = baseProperties();
		EastmoneyRequestProperties requestProperties = requestProperties();
		EastmoneyRetryExecutor retryExecutor = new EastmoneyRetryExecutor(requestProperties, ms -> {});
		Mockito.when(executor.get(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("{\"rc\":1,\"data\":null}");

		EastmoneyKlineClient client = new EastmoneyKlineClient(executor, properties, requestProperties, retryExecutor,
				new EastmoneyKlineParser(new ObjectMapper()));

		Assertions.assertThatThrownBy(() -> client.fetchKlines("000001", "0.000001", "1d", 101, 1,
				LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), null))
				.isInstanceOf(EastmoneyApiException.class);
	}

	@Test
	void throwsWhenDataIsNull() {
		EastmoneyRequestExecutor executor = Mockito.mock(EastmoneyRequestExecutor.class);
		EastmoneyProperties properties = baseProperties();
		EastmoneyRequestProperties requestProperties = requestProperties();
		EastmoneyRetryExecutor retryExecutor = new EastmoneyRetryExecutor(requestProperties, ms -> {});
		Mockito.when(executor.get(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("{\"rc\":0}");

		EastmoneyKlineClient client = new EastmoneyKlineClient(executor, properties, requestProperties, retryExecutor,
				new EastmoneyKlineParser(new ObjectMapper()));

		Assertions.assertThatThrownBy(() -> client.fetchKlines("000001", "0.000001", "1d", 101, 1,
				LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), null))
				.isInstanceOf(EastmoneyApiException.class);
	}

	private EastmoneyProperties baseProperties() {
		EastmoneyProperties properties = new EastmoneyProperties();
		properties.setBaseUrl("https://example.com");
		properties.setFields1("f1");
		properties.setFields2("f2");
		properties.setUt("ut");
		return properties;
	}

	private EastmoneyRequestProperties requestProperties() {
		EastmoneyRequestProperties properties = new EastmoneyRequestProperties();
		properties.setMaxRetries(0);
		properties.setRetryRcCodes(List.of(102));
		return properties;
	}
}
