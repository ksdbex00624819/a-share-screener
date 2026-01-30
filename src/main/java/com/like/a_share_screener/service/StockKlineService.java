package com.like.a_share_screener.service;

import com.like.a_share_screener.domain.Candle;
import com.like.a_share_screener.persistence.entity.StockKlineEntity;
import com.like.a_share_screener.persistence.mapper.StockKlineMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StockKlineService {
	private final StockKlineMapper mapper;

	public StockKlineService(StockKlineMapper mapper) {
		this.mapper = mapper;
	}

	public int upsertBatch(String code, String timeframe, int fqt, List<Candle> candles) {
		if (!StringUtils.hasText(code) || !StringUtils.hasText(timeframe) || candles == null || candles.isEmpty()) {
			return 0;
		}
		List<StockKlineEntity> items = candles.stream()
				.map(candle -> toEntity(code, timeframe, fqt, candle))
				.collect(Collectors.toList());
		return mapper.upsertBatch(items);
	}

	public Optional<LocalDateTime> getLatestBarTime(String code, String timeframe, int fqt) {
		if (!StringUtils.hasText(code) || !StringUtils.hasText(timeframe)) {
			return Optional.empty();
		}
		return Optional.ofNullable(mapper.selectLatestBarTime(code, timeframe, fqt));
	}

	public List<StockKlineEntity> listBars(String code, String timeframe, int fqt, LocalDateTime fromInclusive,
			LocalDateTime toInclusive, Integer limit, boolean asc) {
		if (!StringUtils.hasText(code) || !StringUtils.hasText(timeframe)) {
			return Collections.emptyList();
		}
		return mapper.selectBars(code, timeframe, fqt, fromInclusive, toInclusive, limit, asc);
	}

	private StockKlineEntity toEntity(String code, String timeframe, int fqt, Candle candle) {
		StockKlineEntity entity = new StockKlineEntity();
		entity.setCode(code);
		entity.setTimeframe(timeframe);
		entity.setBarTime(candle.barTime());
		entity.setFqt(fqt);
		entity.setOpen(candle.open());
		entity.setHigh(candle.high());
		entity.setLow(candle.low());
		entity.setClose(candle.close());
		entity.setVolume(candle.volume());
		entity.setAmount(candle.amount());
		entity.setAmplitudePct(candle.amplitudePct());
		entity.setChangePct(candle.changePct());
		entity.setChangeAmt(candle.changeAmt());
		entity.setTurnoverPct(candle.turnoverPct());
		return entity;
	}
}
