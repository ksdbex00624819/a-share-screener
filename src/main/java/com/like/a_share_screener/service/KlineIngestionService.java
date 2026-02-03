package com.like.a_share_screener.service;

import com.like.a_share_screener.client.EastmoneyKlineClient;
import com.like.a_share_screener.domain.Candle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KlineIngestionService {
	private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
	private static final LocalTime DAILY_CLOSE = LocalTime.of(15, 0);
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
	private static final Logger log = LoggerFactory.getLogger(KlineIngestionService.class);
	private final EastmoneyKlineClient client;
	private final StockKlineService klineService;
	private final KlineIngestionProperties properties;

	public KlineIngestionService(EastmoneyKlineClient client, StockKlineService klineService,
			KlineIngestionProperties properties) {
		this.client = client;
		this.klineService = klineService;
		this.properties = properties;
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
				.map(candle -> normalizeTimeframe(candle, timeframe, secid))
				.flatMap(Optional::stream)
				.filter(candle -> latest == null || candle.barTime().isAfter(latest))
				.toList();
		int upserted = klineService.upsertBatch(secid, timeframe, fqt, normalized);
		int retainBars = properties.resolveRetentionBars(timeframe);
		if (upserted > 0 && retainBars > 0) {
			klineService.pruneOldBars(secid, timeframe, fqt, retainBars);
		}
		return upserted;
	}

	private Optional<Candle> normalizeTimeframe(Candle candle, String timeframe, String secid) {
		if ("1d".equals(timeframe) || "1w".equals(timeframe)) {
			return Optional.of(new Candle(
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
			));
		}
		if (!candle.hasTime()) {
			log.error("Missing intraday time for secid={}, timeframe={}, barTime={}", secid, timeframe,
					candle.barTime());
			return Optional.empty();
		}
		return Optional.of(candle);
	}
}
