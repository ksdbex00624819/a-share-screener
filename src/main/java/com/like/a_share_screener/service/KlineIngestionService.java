package com.like.a_share_screener.service;

import com.like.a_share_screener.client.EastmoneyKlineClient;
import com.like.a_share_screener.domain.Candle;
import com.like.a_share_screener.persistence.entity.StockKlineDailyEntity;
import com.like.a_share_screener.persistence.mapper.StockKlineDailyMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class KlineIngestionService {
	private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
	private final EastmoneyKlineClient client;
	private final StockKlineDailyMapper mapper;

	public KlineIngestionService(EastmoneyKlineClient client, StockKlineDailyMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public int ingestDaily(String secid, int fqt, LocalDate beg, LocalDate end) {
		LocalDate latest = mapper.selectLatestTradeDate(secid, fqt);
		LocalDate effectiveBeg = latest == null ? beg : latest.plusDays(1);
		LocalDate effectiveEnd = end == null ? LocalDate.now(CHINA_ZONE) : end;
		if (effectiveBeg.isAfter(effectiveEnd)) {
			return 0;
		}
		List<Candle> candles = client.fetchDailyKlines(secid, effectiveBeg, effectiveEnd, fqt);
		if (candles.isEmpty()) {
			return 0;
		}
		List<StockKlineDailyEntity> entities = candles.stream()
				.map(candle -> toEntity(secid, fqt, candle))
				.collect(Collectors.toList());
		return mapper.upsertBatch(entities);
	}

	private StockKlineDailyEntity toEntity(String secid, int fqt, Candle candle) {
		StockKlineDailyEntity entity = new StockKlineDailyEntity();
		entity.setCode(secid);
		entity.setFqt(fqt);
		entity.setTradeDate(candle.tradeDate());
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
