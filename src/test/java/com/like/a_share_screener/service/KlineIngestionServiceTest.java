package com.like.a_share_screener.service;

import com.like.a_share_screener.client.EastmoneyKlineClient;
import com.like.a_share_screener.domain.Candle;
import com.like.a_share_screener.persistence.mapper.StockKlineDailyMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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
	private StockKlineDailyMapper mapper;

	@Autowired
	private EastmoneyKlineClient client;

	@BeforeEach
	void setUp() {
		mapper.delete(null);
	}

	@Test
	void ingestDailyUsesLatestTradeDateAndUpserts() {
		LocalDate beg = LocalDate.of(2024, 1, 1);
		LocalDate end = LocalDate.of(2024, 1, 3);

		Mockito.when(client.fetchDailyKlines("1.000001", beg, end, 1))
				.thenReturn(List.of(
						candle(LocalDate.of(2024, 1, 2)),
						candle(LocalDate.of(2024, 1, 3))
				));

		int inserted = ingestionService.ingestDaily("1.000001", 1, beg, end);
		Assertions.assertThat(inserted).isEqualTo(2);
		Assertions.assertThat(mapper.selectLatestTradeDate("1.000001", 1))
				.isEqualTo(LocalDate.of(2024, 1, 3));

		LocalDate nextBeg = LocalDate.of(2024, 1, 4);
		Mockito.when(client.fetchDailyKlines("1.000001", nextBeg, LocalDate.of(2024, 1, 4), 1))
				.thenReturn(List.of(candle(LocalDate.of(2024, 1, 4))));

		int nextInsert = ingestionService.ingestDaily("1.000001", 1, beg, LocalDate.of(2024, 1, 4));
		Assertions.assertThat(nextInsert).isEqualTo(1);
		Assertions.assertThat(mapper.selectLatestTradeDate("1.000001", 1))
				.isEqualTo(LocalDate.of(2024, 1, 4));

		Mockito.verify(client).fetchDailyKlines("1.000001", beg, end, 1);
		Mockito.verify(client).fetchDailyKlines("1.000001", nextBeg, LocalDate.of(2024, 1, 4), 1);
	}

	private Candle candle(LocalDate date) {
		return new Candle(
				date,
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
