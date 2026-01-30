package com.like.a_share_screener.service;

import com.like.a_share_screener.persistence.entity.StockFactorDailyEntity;
import com.like.a_share_screener.persistence.entity.StockKlineDailyEntity;
import com.like.a_share_screener.persistence.mapper.StockFactorDailyMapper;
import com.like.a_share_screener.persistence.mapper.StockKlineDailyMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Bar;
import org.ta4j.core.BarBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

@Service
public class FactorComputeService {
	private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
	private final StockKlineDailyMapper klineMapper;
	private final StockFactorDailyMapper factorMapper;
	private final FactorComputationProperties properties;

	public FactorComputeService(StockKlineDailyMapper klineMapper, StockFactorDailyMapper factorMapper,
			FactorComputationProperties properties) {
		this.klineMapper = klineMapper;
		this.factorMapper = factorMapper;
		this.properties = properties;
	}

	public int computeFactorsForCode(String code) {
		return computeFactorsForCode(code, false);
	}

	int computeFactorsForCode(String code, boolean recomputeLatest) {
		LocalDate maxDate = factorMapper.selectLatestTradeDate(code);
		LocalDate effectiveMaxDate = maxDate;
		if (recomputeLatest && maxDate != null) {
			effectiveMaxDate = maxDate.minusDays(1);
		}
		int seedBars = Math.max(1, properties.getSeedBars());
		List<StockKlineDailyEntity> bars = klineMapper.selectRecentByCode(code, seedBars);
		if (bars.isEmpty()) {
			return 0;
		}
		Collections.reverse(bars);
		BarSeries series = new BaseBarSeriesBuilder().withName(code).build();
		for (StockKlineDailyEntity bar : bars) {
			series.addBar(toBar(series, bar));
		}
		ClosePriceIndicator close = new ClosePriceIndicator(series);
		SMAIndicator sma5 = new SMAIndicator(close, 5);
		SMAIndicator sma10 = new SMAIndicator(close, 10);
		SMAIndicator sma20 = new SMAIndicator(close, 20);
		SMAIndicator sma60 = new SMAIndicator(close, 60);
		EMAIndicator ema5 = new EMAIndicator(close, 5);
		EMAIndicator ema10 = new EMAIndicator(close, 10);
		EMAIndicator ema20 = new EMAIndicator(close, 20);
		EMAIndicator ema60 = new EMAIndicator(close, 60);
		RSIIndicator rsi14 = new RSIIndicator(close, 14);
		MACDIndicator macd = new MACDIndicator(close, 12, 26);
		EMAIndicator macdSignal = new EMAIndicator(macd, 9);
		BollingerBandsMiddleIndicator bollMid = new BollingerBandsMiddleIndicator(sma20);
		StandardDeviationIndicator stddev = new StandardDeviationIndicator(close, 20);
		BollingerBandsUpperIndicator bollUp = new BollingerBandsUpperIndicator(bollMid, stddev,
				series.numFactory().numOf(2));
		BollingerBandsLowerIndicator bollLow = new BollingerBandsLowerIndicator(bollMid, stddev,
				series.numFactory().numOf(2));
		StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, 9);
		SMAIndicator kdjK = new SMAIndicator(stochK, 3);
		SMAIndicator kdjD = new SMAIndicator(kdjK, 3);
		List<StockFactorDailyEntity> toUpsert = new ArrayList<>();
		for (int i = 0; i < bars.size(); i++) {
			StockKlineDailyEntity kline = bars.get(i);
			LocalDate tradeDate = kline.getTradeDate();
			if (effectiveMaxDate != null && !tradeDate.isAfter(effectiveMaxDate)) {
				continue;
			}
			Num macdValue = macd.getValue(i);
			Num macdSignalValue = macdSignal.getValue(i);
			boolean macdStable = isStable(i, macd) && isStable(i, macdSignal);
			boolean kdjStable = isStable(i, kdjK) && isStable(i, kdjD);
			Num kdjJ = computeKdjJ(kdjK.getValue(i), kdjD.getValue(i), series);
			StockFactorDailyEntity factor = new StockFactorDailyEntity();
			factor.setCode(code);
			factor.setTradeDate(tradeDate);
			factor.setMa5(toDecimal(sma5, i));
			factor.setMa10(toDecimal(sma10, i));
			factor.setMa20(toDecimal(sma20, i));
			factor.setMa60(toDecimal(sma60, i));
			factor.setEma5(toDecimal(ema5, i));
			factor.setEma10(toDecimal(ema10, i));
			factor.setEma20(toDecimal(ema20, i));
			factor.setEma60(toDecimal(ema60, i));
			factor.setRsi14(toDecimal(rsi14, i));
			factor.setMacd(toDecimal(macd, i));
			factor.setMacdSignal(toDecimal(macdSignal, i));
			factor.setMacdHist(toDecimal(subtract(macdValue, macdSignalValue), macdStable));
			factor.setBollMid(toDecimal(bollMid, i));
			factor.setBollUp(toDecimal(bollUp, i));
			factor.setBollLow(toDecimal(bollLow, i));
			factor.setKdjK(toDecimal(kdjK, i));
			factor.setKdjD(toDecimal(kdjD, i));
			factor.setKdjJ(toDecimal(kdjJ, kdjStable));
			toUpsert.add(factor);
		}
		if (toUpsert.isEmpty()) {
			return 0;
		}
		factorMapper.upsertBatch(toUpsert);
		return toUpsert.size();
	}

	private Bar toBar(BarSeries series, StockKlineDailyEntity bar) {
		ZonedDateTime endTime = bar.getTradeDate().atStartOfDay(CHINA_ZONE);
		BarBuilder builder = series.barBuilder()
				.timePeriod(Duration.ofDays(1))
				.beginTime(endTime.minusDays(1).toInstant())
				.endTime(endTime.toInstant())
				.openPrice(defaultNumber(bar.getOpen()))
				.highPrice(defaultNumber(bar.getHigh()))
				.lowPrice(defaultNumber(bar.getLow()))
				.closePrice(defaultNumber(bar.getClose()))
				.volume(defaultNumber(bar.getVolume()))
				.amount(defaultNumber(bar.getAmount()))
				.trades(0);
		return builder.build();
	}

	private Number defaultNumber(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private Number defaultNumber(Long value) {
		return value == null ? 0L : value;
	}

	private BigDecimal toDecimal(Indicator<Num> indicator, int index) {
		if (!isStable(index, indicator)) {
			return null;
		}
		return toDecimal(indicator.getValue(index));
	}

	private BigDecimal toDecimal(Num value, boolean stable) {
		if (!stable) {
			return null;
		}
		return toDecimal(value);
	}

	private BigDecimal toDecimal(Num value) {
		if (Num.isNaNOrNull(value)) {
			return null;
		}
		return value.bigDecimalValue();
	}

	private boolean isStable(int index, Indicator<?> indicator) {
		return index >= indicator.getCountOfUnstableBars();
	}

	private Num subtract(Num left, Num right) {
		if (left == null || right == null) {
			return null;
		}
		return left.minus(right);
	}

	private Num computeKdjJ(Num k, Num d, BarSeries series) {
		if (k == null || d == null) {
			return null;
		}
		Num three = series.numFactory().three();
		Num two = series.numFactory().two();
		return k.multipliedBy(three).minus(d.multipliedBy(two));
	}
}
