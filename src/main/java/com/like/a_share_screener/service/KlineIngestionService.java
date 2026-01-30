package com.like.a_share_screener.service;

import com.like.a_share_screener.client.EastmoneyKlineClient;
import com.like.a_share_screener.domain.Candle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KlineIngestionService {
	private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
	private static final LocalTime DAILY_CLOSE = LocalTime.of(15, 0);
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
	private final EastmoneyKlineClient client;
	private final StockKlineService klineService;

	public KlineIngestionService(EastmoneyKlineClient client, StockKlineService klineService) {
		this.client = client;
		this.klineService = klineService;
	}

	public int ingest(String secid, String timeframe, int klt, int fqt, String defaultBeg, String defaultEnd,
			LocalDate endDate, int recentLimit) {
		LocalDateTime latest = klineService.getLatestBarTime(secid, timeframe, fqt).orElse(null);
		boolean dailyOrWeekly = "1d".equals(timeframe) || "1w".equals(timeframe);
		String beg = defaultBeg;
		String end = defaultEnd;
		if (dailyOrWeekly) {
			LocalDate effectiveEnd = endDate == null ? LocalDate.now(CHINA_ZONE) : endDate;
			if (latest != null) {
				LocalDate nextDate = latest.toLocalDate().plusDays(1);
				if (nextDate.isAfter(effectiveEnd)) {
					return 0;
				}
				beg = nextDate.format(DATE_FORMAT);
				end = effectiveEnd.format(DATE_FORMAT);
			} else {
				end = effectiveEnd.format(DATE_FORMAT);
			}
		}
		List<Candle> candles = client.fetchKlines(secid, klt, fqt, beg, end, dailyOrWeekly ? null : recentLimit);
		if (candles.isEmpty()) {
			return 0;
		}
		List<Candle> normalized = candles.stream()
				.map(candle -> normalizeTimeframe(candle, timeframe))
				.filter(candle -> latest == null || candle.barTime().isAfter(latest))
				.toList();
		return klineService.upsertBatch(secid, timeframe, fqt, normalized);
	}

	private Candle normalizeTimeframe(Candle candle, String timeframe) {
		if ("1d".equals(timeframe) || "1w".equals(timeframe)) {
			return new Candle(
					candle.barTime().toLocalDate().atTime(DAILY_CLOSE),
					candle.hasTime(),
					candle.open(),
					candle.high(),
					candle.low(),
					candle.close(),
					candle.volume(),
					candle.amount(),
					candle.amplitudePct(),
					candle.changePct(),
					candle.changeAmt(),
					candle.turnoverPct()
			);
		}
		if (!candle.hasTime()) {
			return new Candle(
					candle.barTime().toLocalDate().atTime(DAILY_CLOSE),
					candle.hasTime(),
					candle.open(),
					candle.high(),
					candle.low(),
					candle.close(),
					candle.volume(),
					candle.amount(),
					candle.amplitudePct(),
					candle.changePct(),
					candle.changeAmt(),
					candle.turnoverPct()
			);
		}
		return candle;
	}
}
