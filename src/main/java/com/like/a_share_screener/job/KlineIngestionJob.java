package com.like.a_share_screener.job;

import com.like.a_share_screener.persistence.entity.StockBasicEntity;
import com.like.a_share_screener.client.EastmoneyRequestProperties;
import com.like.a_share_screener.client.EastmoneyNonRetryableException;
import com.like.a_share_screener.client.EastmoneyTransientException;
import com.like.a_share_screener.service.KlineIngestionProperties;
import com.like.a_share_screener.service.KlineIngestionResult;
import com.like.a_share_screener.service.KlineIngestionService;
import com.like.a_share_screener.service.StockBasicService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.lang.Nullable;

@Component
public class KlineIngestionJob {
	private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
	private static final Logger log = LoggerFactory.getLogger(KlineIngestionJob.class);
	private final KlineIngestionService ingestionService;
	private final KlineIngestionProperties properties;
	private final StockBasicService stockBasicService;
	private final EastmoneyRequestProperties requestProperties;

	public KlineIngestionJob(KlineIngestionService ingestionService, KlineIngestionProperties properties,
			StockBasicService stockBasicService, EastmoneyRequestProperties requestProperties) {
		this.ingestionService = ingestionService;
		this.properties = properties;
		this.stockBasicService = stockBasicService;
		this.requestProperties = requestProperties;
	}

	@Scheduled(cron = "${kline.ingestion.cron:0 30 15 * * MON-FRI}", zone = "Asia/Shanghai")
	public void ingestKlines() {
		runOnce(null);
	}

	public JobRunResult runOnce(@Nullable JobRunOverrides overrides) {
		Instant startedAt = Instant.now();
		try {
			List<String> timeframes = overrides != null && overrides.getTimeframes() != null
					? overrides.getTimeframes()
					: properties.getEnabledTimeframes();
			int maxUniverseSize = overrides != null && overrides.getMaxUniverseSize() != null
					? overrides.getMaxUniverseSize()
					: properties.getMaxUniverseSize();
			LocalDate end = LocalDate.now(CHINA_ZONE);
			List<StockBasicEntity> universe = stockBasicService.listMainBoardActive(maxUniverseSize);
			if (universe.isEmpty()) {
				log.warn("Kline ingestion skipped: no MAIN ACTIVE stocks found");
				Instant finishedAt = Instant.now();
				return JobRunResult.success(startedAt, finishedAt, 0, 0, 0);
			}
			int successTotal = 0;
			int failTotal = 0;
			for (String timeframe : timeframes) {
				int klt = properties.resolveKlt(timeframe);
				int successCount = 0;
				int failCount = 0;
				int doneCount = 0;
				Map<String, Integer> rcStats = new LinkedHashMap<>();
				List<String> failedSamples = new ArrayList<>();
				for (StockBasicEntity stock : universe) {
					String secid = stock.getSecid();
					String code = stock.getCode();
					long startTime = System.nanoTime();
					doneCount++;
					try {
						KlineIngestionResult result = ingestionService.ingest(code, secid, timeframe, klt,
								properties.getFqt(), properties.getDefaultBeg(), properties.getDefaultEnd(), end,
								properties.getRecentLimit());
						successCount++;
						if (requestProperties.isLogSuccess()
								&& shouldLogEvery(doneCount, requestProperties.getLogSuccessEveryN())) {
							long costMs = (System.nanoTime() - startTime) / 1_000_000;
							log.info("kline_fetch_ok code={} secid={} timeframe={} fqt={} bars={} first={} last={} costMs={}",
									code, secid, timeframe, properties.getFqt(), result.fetchedBars(),
									result.firstBarTime(), result.lastBarTime(), costMs);
						}
					} catch (Exception ex) {
						failCount++;
						if (failedSamples.size() < 20) {
							failedSamples.add(code);
						}
						String reason = classifyFailure(ex);
						rcStats.merge(reason, 1, Integer::sum);
					}
					if (shouldLogEvery(doneCount, requestProperties.getLogProgressEveryN())) {
						log.info("kline_ingestion_progress done={}/{} ok={} fail={}",
								doneCount, universe.size(), successCount, failCount);
					}
				}
				successTotal += successCount;
				failTotal += failCount;
				log.info("kline_ingestion_summary total={} okSymbols={} failSymbols={} rcStats={} failedSamples={}",
						universe.size(), successCount, failCount, rcStats, String.join(",", failedSamples));
			}
			int totalSymbols = universe.size() * timeframes.size();
			Instant finishedAt = Instant.now();
			return JobRunResult.success(startedAt, finishedAt, totalSymbols, successTotal, failTotal);
		} catch (Exception ex) {
			Instant finishedAt = Instant.now();
			log.error("Kline ingestion failed", ex);
			return JobRunResult.failure(startedAt, finishedAt, ex.getMessage());
		}
	}

	private boolean shouldLogEvery(int index, int everyN) {
		if (everyN <= 0) {
			return false;
		}
		return index % everyN == 0;
	}

	private String classifyFailure(Throwable ex) {
		if (ex instanceof EastmoneyTransientException transientException) {
			Integer rc = transientException.getRc();
			return rc == null ? "rc_unknown" : String.valueOf(rc);
		}
		if (ex instanceof EastmoneyNonRetryableException nonRetryableException) {
			Integer rc = nonRetryableException.getRc();
			return rc == null ? "non_retryable" : String.valueOf(rc);
		}
		if (ex instanceof RestClientResponseException responseException) {
			return "http_" + responseException.getStatusCode().value();
		}
		if (ex instanceof RestClientException restClientException) {
			Throwable cause = restClientException.getCause();
			if (cause instanceof java.net.SocketTimeoutException) {
				return "io_timeout";
			}
			if (cause instanceof java.io.IOException) {
				return "io_error";
			}
			return "io_unknown";
		}
		return ex.getClass().getSimpleName();
	}
}
