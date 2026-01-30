package com.like.a_share_screener.service;

import com.like.a_share_screener.domain.FactorRow;
import com.like.a_share_screener.persistence.entity.StockFactorEntity;
import com.like.a_share_screener.persistence.mapper.StockFactorMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StockFactorService {
	private final StockFactorMapper mapper;

	public StockFactorService(StockFactorMapper mapper) {
		this.mapper = mapper;
	}

	public Optional<LocalDateTime> getLatestBarTime(String code, String timeframe, int fqt) {
		if (!StringUtils.hasText(code) || !StringUtils.hasText(timeframe)) {
			return Optional.empty();
		}
		return Optional.ofNullable(mapper.selectLatestBarTime(code, timeframe, fqt));
	}

	public int upsertBatch(String code, String timeframe, int fqt, List<FactorRow> rows) {
		if (!StringUtils.hasText(code) || !StringUtils.hasText(timeframe) || rows == null || rows.isEmpty()) {
			return 0;
		}
		List<StockFactorEntity> items = rows.stream()
				.map(row -> toEntity(code, timeframe, fqt, row))
				.collect(Collectors.toList());
		return mapper.upsertBatch(items);
	}

	private StockFactorEntity toEntity(String code, String timeframe, int fqt, FactorRow row) {
		StockFactorEntity entity = new StockFactorEntity();
		entity.setCode(code);
		entity.setTimeframe(timeframe);
		entity.setBarTime(row.barTime());
		entity.setFqt(fqt);
		entity.setMa5(row.ma5());
		entity.setMa10(row.ma10());
		entity.setMa20(row.ma20());
		entity.setMa60(row.ma60());
		entity.setEma5(row.ema5());
		entity.setEma10(row.ema10());
		entity.setEma20(row.ema20());
		entity.setEma60(row.ema60());
		entity.setRsi14(row.rsi14());
		entity.setMacd(row.macd());
		entity.setMacdSignal(row.macdSignal());
		entity.setMacdHist(row.macdHist());
		entity.setBollMid(row.bollMid());
		entity.setBollUp(row.bollUp());
		entity.setBollLow(row.bollLow());
		entity.setKdjK(row.kdjK());
		entity.setKdjD(row.kdjD());
		entity.setKdjJ(row.kdjJ());
		entity.setVolMa5(row.volMa5());
		entity.setVolMa10(row.volMa10());
		entity.setVolMa20(row.volMa20());
		entity.setVolMa60(row.volMa60());
		entity.setAmtMa20(row.amtMa20());
		entity.setVolRatio20(row.volRatio20());
		return entity;
	}
}
