package com.like.a_share_screener.service;

import com.like.a_share_screener.domain.FactorRow;
import com.like.a_share_screener.persistence.entity.StockKlineEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Bar;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractIndicator;
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
	private final StockKlineService klineService;
	private final StockFactorService factorService;
	private final FactorComputationProperties properties;

	public FactorComputeService(StockKlineService klineService, StockFactorService factorService,
			FactorComputationProperties properties) {
		this.klineService = klineService;
		this.factorService = factorService;
		this.properties = properties;
	}

	public int computeFactorsForCode(String code, String timeframe) {
		return computeFactorsForCode(code, timeframe, false);
	}

	int computeFactorsForCode(String code, String timeframe, boolean recomputeLatest) {
		LocalDateTime maxTime = factorService.getLatestBarTime(code, timeframe, properties.getFqt()).orElse(null);
		LocalDateTime effectiveMaxTime = maxTime;
		if (recomputeLatest && maxTime != null) {
			effectiveMaxTime = maxTime.minusSeconds(1);
		}
		int seedBars = Math.max(1, properties.resolveSeedBars(timeframe));
		List<StockKlineEntity> bars = klineService.listBars(code, timeframe, properties.getFqt(), null, null,
				seedBars, false);
		if (bars.isEmpty()) {
			return 0;
		}
		Collections.reverse(bars);
		BarSeries series = new BaseBarSeriesBuilder().withName(code + "-" + timeframe).build();
		for (StockKlineEntity bar : bars) {
			series.addBar(toBar(series, bar, timeframe));
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
		BarVolumeIndicator volumeIndicator = new BarVolumeIndicator(series);
		BarAmountIndicator amountIndicator = new BarAmountIndicator(series);
		SMAIndicator volMa5 = new SMAIndicator(volumeIndicator, 5);
		SMAIndicator volMa10 = new SMAIndicator(volumeIndicator, 10);
		SMAIndicator volMa20 = new SMAIndicator(volumeIndicator, 20);
		SMAIndicator volMa60 = new SMAIndicator(volumeIndicator, 60);
		SMAIndicator amtMa20 = new SMAIndicator(amountIndicator, 20);
		List<FactorRow> toUpsert = new ArrayList<>();
		for (int i = 0; i < bars.size(); i++) {
			StockKlineEntity kline = bars.get(i);
			LocalDateTime barTime = kline.getBarTime();
			if (effectiveMaxTime != null && !barTime.isAfter(effectiveMaxTime)) {
				continue;
			}
			Num macdValue = macd.getValue(i);
			Num macdSignalValue = macdSignal.getValue(i);
			boolean macdStable = isStable(i, macd) && isStable(i, macdSignal);
			boolean kdjStable = isStable(i, kdjK) && isStable(i, kdjD);
			Num kdjJ = computeKdjJ(kdjK.getValue(i), kdjD.getValue(i), series);
			BigDecimal volMa20Value = toDecimal(volMa20, i);
			BigDecimal volRatio20 = computeVolRatio(kline.getVolume(), volMa20Value);
			FactorRow factor = new FactorRow(
					barTime,
					toDecimal(sma5, i),
					toDecimal(sma10, i),
					toDecimal(sma20, i),
					toDecimal(sma60, i),
					toDecimal(ema5, i),
					toDecimal(ema10, i),
					toDecimal(ema20, i),
					toDecimal(ema60, i),
					toDecimal(rsi14, i),
					toDecimal(macd, i),
					toDecimal(macdSignal, i),
					toDecimal(subtract(macdValue, macdSignalValue), macdStable),
					toDecimal(bollMid, i),
					toDecimal(bollUp, i),
					toDecimal(bollLow, i),
					toDecimal(kdjK, i),
					toDecimal(kdjD, i),
					toDecimal(kdjJ, kdjStable),
					toDecimal(volMa5, i),
					toDecimal(volMa10, i),
					volMa20Value,
					toDecimal(volMa60, i),
					toDecimal(amtMa20, i),
					volRatio20
			);
			toUpsert.add(factor);
		}
		if (toUpsert.isEmpty()) {
			return 0;
		}
		factorService.upsertBatch(code, timeframe, properties.getFqt(), toUpsert);
		return toUpsert.size();
	}

	private Bar toBar(BarSeries series, StockKlineEntity bar, String timeframe) {
		ZonedDateTime endTime = bar.getBarTime().atZone(CHINA_ZONE);
		Duration duration = resolveDuration(timeframe);
		return series.barBuilder()
				.timePeriod(duration)
				.beginTime(endTime.minus(duration).toInstant())
				.endTime(endTime.toInstant())
				.openPrice(defaultNumber(bar.getOpen()))
				.highPrice(defaultNumber(bar.getHigh()))
				.lowPrice(defaultNumber(bar.getLow()))
				.closePrice(defaultNumber(bar.getClose()))
				.volume(defaultNumber(bar.getVolume()))
				.amount(defaultNumber(bar.getAmount()))
				.trades(0)
				.build();
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

	private Duration resolveDuration(String timeframe) {
		if (timeframe.endsWith("m")) {
			int minutes = Integer.parseInt(timeframe.substring(0, timeframe.length() - 1));
			return Duration.ofMinutes(minutes);
		}
		if (timeframe.endsWith("w")) {
			int weeks = Integer.parseInt(timeframe.substring(0, timeframe.length() - 1));
			return Duration.ofDays(weeks * 7L);
		}
		if (timeframe.endsWith("d")) {
			int days = Integer.parseInt(timeframe.substring(0, timeframe.length() - 1));
			return Duration.ofDays(days);
		}
		return Duration.ofDays(1);
	}

	private BigDecimal computeVolRatio(Long volume, BigDecimal volMa20) {
		if (volume == null || volMa20 == null || volMa20.compareTo(BigDecimal.ZERO) == 0) {
			return null;
		}
		return BigDecimal.valueOf(volume).divide(volMa20, 8, RoundingMode.HALF_UP);
	}

	private static class BarVolumeIndicator extends AbstractIndicator<Num> {
		private final BarSeries series;

		BarVolumeIndicator(BarSeries series) {
			super(series);
			this.series = series;
		}

		@Override
		public Num getValue(int index) {
			return series.getBar(index).getVolume();
		}

		@Override
		public int getCountOfUnstableBars() {
			return 0;
		}
	}

	private static class BarAmountIndicator extends AbstractIndicator<Num> {
		private final BarSeries series;

		BarAmountIndicator(BarSeries series) {
			super(series);
			this.series = series;
		}

		@Override
		public Num getValue(int index) {
			return series.getBar(index).getAmount();
		}

		@Override
		public int getCountOfUnstableBars() {
			return 0;
		}
	}
}
