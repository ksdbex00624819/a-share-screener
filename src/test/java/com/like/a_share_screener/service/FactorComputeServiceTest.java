package com.like.a_share_screener.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.like.a_share_screener.persistence.entity.StockFactorEntity;
import com.like.a_share_screener.persistence.entity.StockKlineEntity;
import com.like.a_share_screener.persistence.mapper.StockFactorMapper;
import com.like.a_share_screener.persistence.mapper.StockKlineMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FactorComputeServiceTest {
	@Autowired
	private FactorComputeService computeService;
	@Autowired
	private StockKlineMapper klineMapper;
	@Autowired
	private StockFactorMapper factorMapper;

	@BeforeEach
	void setUp() {
		factorMapper.delete(null);
		klineMapper.delete(null);
	}

	@Test
	void volumeMaComputedAcrossTimeframes() {
		String code = "000001";
		klineMapper.upsertBatch(buildKlines(code, "1d", LocalDate.of(2024, 1, 1).atTime(15, 0), 30, 1000));
		klineMapper.upsertBatch(buildKlines(code, "60m", LocalDateTime.of(2024, 1, 1, 9, 30), 30, 2000));

		computeService.computeFactorsForCode(code, "1d");
		computeService.computeFactorsForCode(code, "60m");

		List<StockFactorEntity> daily = factorMapper.selectList(
				new LambdaQueryWrapper<StockFactorEntity>()
						.eq(StockFactorEntity::getCode, code)
						.eq(StockFactorEntity::getTimeframe, "1d")
						.orderByAsc(StockFactorEntity::getBarTime));
		List<StockFactorEntity> hourly = factorMapper.selectList(
				new LambdaQueryWrapper<StockFactorEntity>()
						.eq(StockFactorEntity::getCode, code)
						.eq(StockFactorEntity::getTimeframe, "60m")
						.orderByAsc(StockFactorEntity::getBarTime));

		assertThat(daily).hasSize(30);
		assertThat(hourly).hasSize(30);

		BigDecimal expectedDailyVolMa20 = averageVolume(1000, 19);
		assertThat(daily.get(19).getVolMa20()).isEqualByComparingTo(expectedDailyVolMa20);
		assertThat(daily.get(19).getVolRatio20()).isNotNull();

		BigDecimal expectedHourlyVolMa20 = averageVolume(2000, 19);
		assertThat(hourly.get(19).getVolMa20()).isEqualByComparingTo(expectedHourlyVolMa20);
		assertThat(hourly.get(19).getVolRatio20()).isNotNull();
	}

	@Test
	void incrementalComputationByTimeframe() {
		String code = "000002";
		klineMapper.upsertBatch(buildKlines(code, "1d", LocalDate.of(2024, 2, 1).atTime(15, 0), 10, 500));
		klineMapper.upsertBatch(buildKlines(code, "60m", LocalDateTime.of(2024, 2, 1, 9, 30), 10, 800));

		computeService.computeFactorsForCode(code, "1d");
		long dailyCount = countFactors(code, "1d");

		klineMapper.upsertBatch(buildKlines(code, "1d", LocalDate.of(2024, 2, 11).atTime(15, 0), 1, 800));
		computeService.computeFactorsForCode(code, "1d");
		long dailyUpdated = countFactors(code, "1d");

		klineMapper.upsertBatch(buildKlines(code, "60m", LocalDateTime.of(2024, 2, 1, 19, 30), 1, 900));
		computeService.computeFactorsForCode(code, "60m");
		long hourlyUpdated = countFactors(code, "60m");

		assertThat(dailyUpdated).isEqualTo(dailyCount + 1);
		assertThat(hourlyUpdated).isEqualTo(11);
		assertThat(countFactors(code, "1d")).isEqualTo(dailyUpdated);
	}

	@Test
	void upsertAvoidsDuplicates() {
		String code = "000003";
		LocalDateTime barTime = LocalDate.of(2024, 3, 1).atTime(15, 0);
		StockKlineEntity entity = buildKline(code, "1d", barTime, 1000L);
		klineMapper.upsertBatch(List.of(entity));
		klineMapper.upsertBatch(List.of(entity));

		long count = klineMapper.selectCount(new LambdaQueryWrapper<StockKlineEntity>()
				.eq(StockKlineEntity::getCode, code)
				.eq(StockKlineEntity::getTimeframe, "1d")
				.eq(StockKlineEntity::getBarTime, barTime)
				.eq(StockKlineEntity::getFqt, 1));
		assertThat(count).isEqualTo(1);
	}

	@Test
	void warmupProducesNullIndicators() {
		String code = "000004";
		klineMapper.upsertBatch(buildKlines(code, "1d", LocalDate.of(2024, 4, 1).atTime(15, 0), 10, 100));

		computeService.computeFactorsForCode(code, "1d");

		List<StockFactorEntity> factors = factorMapper.selectList(
				new LambdaQueryWrapper<StockFactorEntity>()
						.eq(StockFactorEntity::getCode, code)
						.eq(StockFactorEntity::getTimeframe, "1d")
						.orderByAsc(StockFactorEntity::getBarTime));
		assertThat(factors).hasSize(10);
		assertThat(factors.get(0).getMa20()).isNull();
		assertThat(factors.get(0).getVolMa20()).isNull();
		assertThat(factors.get(0).getAtr14()).isNull();
	}

	@Test
	void atrComputedForMultipleTimeframes() {
		String code = "000005";
		klineMapper.upsertBatch(buildKlines(code, "1d", LocalDate.of(2024, 5, 1).atTime(15, 0), 20, 100));
		klineMapper.upsertBatch(buildKlines(code, "60m", LocalDateTime.of(2024, 5, 1, 9, 30), 20, 200));

		computeService.computeFactorsForCode(code, "1d");
		computeService.computeFactorsForCode(code, "60m");

		List<StockFactorEntity> daily = factorMapper.selectList(
				new LambdaQueryWrapper<StockFactorEntity>()
						.eq(StockFactorEntity::getCode, code)
						.eq(StockFactorEntity::getTimeframe, "1d")
						.orderByAsc(StockFactorEntity::getBarTime));
		List<StockFactorEntity> hourly = factorMapper.selectList(
				new LambdaQueryWrapper<StockFactorEntity>()
						.eq(StockFactorEntity::getCode, code)
						.eq(StockFactorEntity::getTimeframe, "60m")
						.orderByAsc(StockFactorEntity::getBarTime));

		assertThat(daily).hasSize(20);
		assertThat(hourly).hasSize(20);
		assertThat(daily.get(13).getAtr14()).isNull();
		assertThat(daily.get(14).getAtr14()).isNotNull().isPositive();
		assertThat(hourly.get(13).getAtr14()).isNull();
		assertThat(hourly.get(14).getAtr14()).isNotNull().isPositive();
	}

	private long countFactors(String code, String timeframe) {
		return factorMapper.selectCount(new LambdaQueryWrapper<StockFactorEntity>()
				.eq(StockFactorEntity::getCode, code)
				.eq(StockFactorEntity::getTimeframe, timeframe));
	}

	private List<StockKlineEntity> buildKlines(String code, String timeframe, LocalDateTime start, int bars,
			long volumeStart) {
		List<StockKlineEntity> items = new ArrayList<>();
		LocalDateTime barTime = start;
		for (int i = 0; i < bars; i++) {
			items.add(buildKline(code, timeframe, barTime, volumeStart + i));
			barTime = nextBarTime(barTime, timeframe);
		}
		return items;
	}

	private StockKlineEntity buildKline(String code, String timeframe, LocalDateTime barTime, long volume) {
		BigDecimal base = BigDecimal.valueOf(10 + (volume % 10));
		StockKlineEntity entity = new StockKlineEntity();
		entity.setCode(code);
		entity.setTimeframe(timeframe);
		entity.setBarTime(barTime);
		entity.setOpen(base);
		entity.setHigh(base.add(BigDecimal.ONE));
		entity.setLow(base.subtract(BigDecimal.ONE));
		entity.setClose(base.add(BigDecimal.valueOf(0.5)));
		entity.setVolume(volume);
		entity.setAmount(base.multiply(BigDecimal.valueOf(1000)));
		entity.setFqt(1);
		return entity;
	}

	private LocalDateTime nextBarTime(LocalDateTime current, String timeframe) {
		if ("60m".equals(timeframe)) {
			return current.plusHours(1);
		}
		if ("1w".equals(timeframe)) {
			return current.plusDays(7);
		}
		return current.plusDays(1).with(LocalTime.of(15, 0));
	}

	private BigDecimal averageVolume(long startVolume, int endIndex) {
		long sum = 0;
		for (int i = 0; i <= endIndex; i++) {
			sum += startVolume + i;
		}
		return BigDecimal.valueOf(sum)
				.divide(BigDecimal.valueOf(20), 8, RoundingMode.HALF_UP);
	}
}
