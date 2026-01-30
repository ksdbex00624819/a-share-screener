package com.like.a_share_screener.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.like.a_share_screener.persistence.entity.StockFactorDailyEntity;
import com.like.a_share_screener.persistence.entity.StockKlineDailyEntity;
import com.like.a_share_screener.persistence.mapper.StockFactorDailyMapper;
import com.like.a_share_screener.persistence.mapper.StockKlineDailyMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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
	private StockKlineDailyMapper klineMapper;
	@Autowired
	private StockFactorDailyMapper factorMapper;

	@BeforeEach
	void setUp() {
		factorMapper.delete(null);
		klineMapper.delete(null);
	}

	@Test
	void warmupAndFiniteValues() {
		String code = "000001";
		LocalDate start = LocalDate.of(2024, 1, 1);
		klineMapper.upsertBatch(buildKlines(code, start, 30));

		computeService.computeFactorsForCode(code);

		List<StockFactorDailyEntity> factors = factorMapper.selectList(
				new LambdaQueryWrapper<StockFactorDailyEntity>()
						.eq(StockFactorDailyEntity::getCode, code)
						.orderByAsc(StockFactorDailyEntity::getTradeDate));
		assertThat(factors).hasSize(30);
		assertThat(factors.get(0).getMa5()).isNull();
		assertThat(factors.get(0).getRsi14()).isNull();
		StockFactorDailyEntity last = factors.get(factors.size() - 1);
		assertThat(last.getMa5()).isNotNull();
		assertThat(last.getRsi14()).isNotNull();
		assertThat(last.getBollMid()).isNotNull();
	}

	@Test
	void incrementalComputationAddsOnlyNewRows() {
		String code = "000002";
		LocalDate start = LocalDate.of(2024, 2, 1);
		klineMapper.upsertBatch(buildKlines(code, start, 10));

		computeService.computeFactorsForCode(code);
		long initialCount = factorMapper.selectCount(new LambdaQueryWrapper<StockFactorDailyEntity>()
				.eq(StockFactorDailyEntity::getCode, code));

		klineMapper.upsertBatch(buildKlines(code, start.plusDays(10), 1));
		computeService.computeFactorsForCode(code);
		long updatedCount = factorMapper.selectCount(new LambdaQueryWrapper<StockFactorDailyEntity>()
				.eq(StockFactorDailyEntity::getCode, code));

		assertThat(updatedCount).isEqualTo(initialCount + 1);
	}

	@Test
	void upsertUpdatesExistingRow() {
		String code = "000003";
		LocalDate start = LocalDate.of(2024, 3, 1);
		klineMapper.upsertBatch(buildKlines(code, start, 25));
		computeService.computeFactorsForCode(code);

		StockFactorDailyEntity before = factorMapper.selectList(
				new LambdaQueryWrapper<StockFactorDailyEntity>()
						.eq(StockFactorDailyEntity::getCode, code)
						.orderByDesc(StockFactorDailyEntity::getTradeDate)
						.last("limit 1"))
				.get(0);

		LocalDate lastDate = start.plusDays(24);
		UpdateWrapper<StockKlineDailyEntity> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("code", code).eq("trade_date", lastDate);
		StockKlineDailyEntity update = new StockKlineDailyEntity();
		update.setClose(new BigDecimal("999.99"));
		klineMapper.update(update, updateWrapper);

		computeService.computeFactorsForCode(code, true);

		StockFactorDailyEntity after = factorMapper.selectList(
				new LambdaQueryWrapper<StockFactorDailyEntity>()
						.eq(StockFactorDailyEntity::getCode, code)
						.orderByDesc(StockFactorDailyEntity::getTradeDate)
						.last("limit 1"))
				.get(0);

		long count = factorMapper.selectCount(new LambdaQueryWrapper<StockFactorDailyEntity>()
				.eq(StockFactorDailyEntity::getCode, code));
		assertThat(count).isEqualTo(25);
		assertThat(after.getEma5()).isNotNull();
		assertThat(after.getEma5()).isNotEqualTo(before.getEma5());
	}

	private List<StockKlineDailyEntity> buildKlines(String code, LocalDate start, int days) {
		List<StockKlineDailyEntity> items = new ArrayList<>();
		for (int i = 0; i < days; i++) {
			LocalDate date = start.plusDays(i);
			BigDecimal base = BigDecimal.valueOf(10 + i);
			StockKlineDailyEntity entity = new StockKlineDailyEntity();
			entity.setCode(code);
			entity.setTradeDate(date);
			entity.setOpen(base);
			entity.setHigh(base.add(BigDecimal.ONE));
			entity.setLow(base.subtract(BigDecimal.ONE));
			entity.setClose(base.add(BigDecimal.valueOf(0.5)));
			entity.setVolume(1000L + i);
			entity.setAmount(base.multiply(BigDecimal.valueOf(1000)));
			entity.setFqt(1);
			items.add(entity);
		}
		return items;
	}
}
