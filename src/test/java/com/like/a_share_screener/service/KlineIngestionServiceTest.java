package com.like.a_share_screener.service;

import com.like.a_share_screener.client.EastmoneyKlineClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.like.a_share_screener.client.EastmoneyKlineParser;
import com.like.a_share_screener.domain.Candle;
import com.like.a_share_screener.persistence.entity.StockKlineEntity;
import com.like.a_share_screener.persistence.mapper.StockKlineMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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

	@Test
	void ingestHourlyKeepsTimeOfDay() {
		EastmoneyKlineParser parser = new EastmoneyKlineParser(new ObjectMapper());
		List<Candle> candles = List.of(
				parser.parseKlineLine("2026-02-02 10:30,10,10,10,9,100,1000,1,1,0.1,0.1"),
				parser.parseKlineLine("2026-02-02 11:30,10,10,10,9,110,1100,1,1,0.1,0.1"),
				parser.parseKlineLine("2026-02-02 14:00,10,10,10,9,120,1200,1,1,0.1,0.1")
		);
		Mockito.when(client.fetchKlines("1.000001", 60, 1, "20240101", "20500101", 120))
				.thenReturn(candles);

		int inserted = ingestionService.ingest("1.000001", "60m", 60, 1, "20240101", "20500101", null, 120);
		Assertions.assertThat(inserted).isEqualTo(3);

		List<StockKlineEntity> stored = mapper.selectList(null).stream()
				.filter(entity -> "60m".equals(entity.getTimeframe()))
				.sorted(Comparator.comparing(StockKlineEntity::getBarTime))
				.toList();
		Assertions.assertThat(stored).hasSize(3);
		Assertions.assertThat(stored.stream()
				.map(StockKlineEntity::getBarTime)
				.collect(Collectors.toSet()))
				.hasSize(3);
		Assertions.assertThat(stored.get(0).getBarTime()).isEqualTo(LocalDateTime.of(2026, 2, 2, 10, 30));
		Assertions.assertThat(stored.get(1).getBarTime()).isEqualTo(LocalDateTime.of(2026, 2, 2, 11, 30));
		Assertions.assertThat(stored.get(2).getBarTime()).isEqualTo(LocalDateTime.of(2026, 2, 2, 14, 0));
	}

	@Test
	void ingestWeeklySetsCloseTime() {
		EastmoneyKlineParser parser = new EastmoneyKlineParser(new ObjectMapper());
		Mockito.when(client.fetchKlines("1.000001", 102, 1, "0", "20240131", null))
				.thenReturn(List.of(
						parser.parseKlineLine("2024-01-19,10,10,10,9,100,1000,1,1,0.1,0.1"),
						parser.parseKlineLine("2024-01-26,10,10,10,9,100,1000,1,1,0.1,0.1")
				));

		int inserted = ingestionService.ingest("1.000001", "1w", 102, 1, "0", "20500101",
				LocalDate.of(2024, 1, 31), 120);
		Assertions.assertThat(inserted).isEqualTo(2);
		Assertions.assertThat(mapper.selectLatestBarTime("1.000001", "1w", 1))
				.isEqualTo(LocalDateTime.of(2024, 1, 26, 15, 0));
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
