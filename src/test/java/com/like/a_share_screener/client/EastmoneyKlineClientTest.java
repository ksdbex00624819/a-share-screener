package com.like.a_share_screener.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EastmoneyKlineClientTest {

	@Test
	void throwsWhenRcIsNonZero() {
		EastmoneyRequestExecutor executor = Mockito.mock(EastmoneyRequestExecutor.class);
		EastmoneyProperties properties = baseProperties();
		Mockito.when(executor.get(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("{\"rc\":1,\"data\":null}");

		EastmoneyKlineClient client = new EastmoneyKlineClient(executor, properties,
				new EastmoneyKlineParser(new ObjectMapper()));

		Assertions.assertThatThrownBy(() -> client.fetchKlines("0.000001", 101, 1,
				LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), null))
				.isInstanceOf(EastmoneyApiException.class);
	}

	@Test
	void throwsWhenDataIsNull() {
		EastmoneyRequestExecutor executor = Mockito.mock(EastmoneyRequestExecutor.class);
		EastmoneyProperties properties = baseProperties();
		Mockito.when(executor.get(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("{\"rc\":0}");

		EastmoneyKlineClient client = new EastmoneyKlineClient(executor, properties,
				new EastmoneyKlineParser(new ObjectMapper()));

		Assertions.assertThatThrownBy(() -> client.fetchKlines("0.000001", 101, 1,
				LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), null))
				.isInstanceOf(EastmoneyApiException.class);
	}

	private EastmoneyProperties baseProperties() {
		EastmoneyProperties properties = new EastmoneyProperties();
		properties.setBaseUrl("https://example.com");
		properties.setFields1("f1");
		properties.setFields2("f2");
		properties.setUt("ut");
		properties.getRequest().setMaxRetries(0);
		return properties;
	}
}
