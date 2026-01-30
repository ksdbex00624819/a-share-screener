package com.like.a_share_screener.service;

import com.like.a_share_screener.client.EastmoneyKlineClient;
import com.like.a_share_screener.domain.Candle;
import com.like.a_share_screener.persistence.mapper.StockKlineMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(KlineIngestionServiceTest.TestConfig.class)
class KlineIngestionServiceTest {

	@Autowired
	private KlineIngestionService ingestionService;

	@Autowired
	private StockKlineMapper mapper;

	@Autowired
	private EastmoneyKlineClient client;

	@BeforeEach
	void setUp() {
		mapper.delete(null);
	}

	@Test
	void ingestDailyUsesLatestTradeDateAndUpserts() {
		LocalDate end = LocalDate.of(2024, 1, 3);

		Mockito.when(client.fetchKlines("1.000001", 101, 1, "0", "20240103", null))
				.thenReturn(List.of(
						candle(LocalDate.of(2024, 1, 2)),
						candle(LocalDate.of(2024, 1, 3))
				));

		int inserted = ingestionService.ingest("1.000001", "1d", 101, 1, "0", "20500101", end, 120);
		Assertions.assertThat(inserted).isEqualTo(2);
		Assertions.assertThat(mapper.selectLatestBarTime("1.000001", "1d", 1))
				.isEqualTo(LocalDateTime.of(2024, 1, 3, 15, 0));

		Mockito.when(client.fetchKlines("1.000001", 101, 1, "20240104", "20240104", null))
				.thenReturn(List.of(candle(LocalDate.of(2024, 1, 4))));

		int nextInsert = ingestionService.ingest("1.000001", "1d", 101, 1, "0", "20500101",
				LocalDate.of(2024, 1, 4), 120);
		Assertions.assertThat(nextInsert).isEqualTo(1);
		Assertions.assertThat(mapper.selectLatestBarTime("1.000001", "1d", 1))
				.isEqualTo(LocalDateTime.of(2024, 1, 4, 15, 0));

		Mockito.verify(client).fetchKlines("1.000001", 101, 1, "0", "20240103", null);
		Mockito.verify(client).fetchKlines("1.000001", 101, 1, "20240104", "20240104", null);
	}

	private Candle candle(LocalDate date) {
		return new Candle(
				LocalDateTime.of(date, LocalTime.NOON),
				true,
				new BigDecimal("10.00"),
				new BigDecimal("10.50"),
				new BigDecimal("9.80"),
				new BigDecimal("10.20"),
				1000L,
				new BigDecimal("10000.00"),
				new BigDecimal("2.00"),
				new BigDecimal("1.00"),
				new BigDecimal("0.10"),
				new BigDecimal("0.50")
		);
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		EastmoneyKlineClient eastmoneyKlineClient() {
			return Mockito.mock(EastmoneyKlineClient.class);
		}
	}
}
