package com.like.a_share_screener.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.like.a_share_screener.domain.Candle;
import com.like.a_share_screener.persistence.entity.StockKlineEntity;
import com.like.a_share_screener.persistence.mapper.StockKlineMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class StockKlineServiceTest {

	@Autowired
	private StockKlineService klineService;

	@Autowired
	private StockKlineMapper klineMapper;

	@BeforeEach
	void setUp() {
		klineMapper.delete(null);
	}

	@Test
	void pruneOldBarsKeepsMostRecent() {
		String code = "000010";
		List<Candle> candles = new ArrayList<>();
		LocalDateTime start = LocalDateTime.of(2024, 1, 2, 9, 30);
		for (int i = 0; i < 8; i++) {
			candles.add(candleAt(start.plusHours(i)));
		}
		klineService.upsertBatch(code, "60m", 1, candles);

		int deleted = klineService.pruneOldBars(code, "60m", 1, 5);
		Assertions.assertThat(deleted).isEqualTo(3);

		List<StockKlineEntity> remaining = klineMapper.selectList(
				new LambdaQueryWrapper<StockKlineEntity>()
						.eq(StockKlineEntity::getCode, code)
						.eq(StockKlineEntity::getTimeframe, "60m")
						.orderByAsc(StockKlineEntity::getBarTime));
		Assertions.assertThat(remaining).hasSize(5);
		Assertions.assertThat(remaining.get(0).getBarTime()).isEqualTo(start.plusHours(3));
		Assertions.assertThat(remaining.get(4).getBarTime()).isEqualTo(start.plusHours(7));
	}

	private Candle candleAt(LocalDateTime time) {
		return new Candle(
				time,
				true,
				new BigDecimal("10"),
				new BigDecimal("11"),
				new BigDecimal("9"),
				new BigDecimal("10.5"),
				1000L,
				new BigDecimal("10000"),
				new BigDecimal("1.0"),
				new BigDecimal("0.5"),
				new BigDecimal("0.1"),
				new BigDecimal("0.2")
		);
	}
}
