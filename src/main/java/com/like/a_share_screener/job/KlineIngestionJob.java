package com.like.a_share_screener.job;

import com.like.a_share_screener.persistence.entity.StockBasicEntity;
import com.like.a_share_screener.service.KlineIngestionProperties;
import com.like.a_share_screener.service.KlineIngestionService;
import com.like.a_share_screener.service.StockBasicService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KlineIngestionJob {
	private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
	private static final Logger log = LoggerFactory.getLogger(KlineIngestionJob.class);
	private final KlineIngestionService ingestionService;
	private final KlineIngestionProperties properties;
	private final StockBasicService stockBasicService;

	public KlineIngestionJob(KlineIngestionService ingestionService, KlineIngestionProperties properties,
			StockBasicService stockBasicService) {
		this.ingestionService = ingestionService;
		this.properties = properties;
		this.stockBasicService = stockBasicService;
	}

	@Scheduled(cron = "${kline.ingestion.cron:0 30 15 * * MON-FRI}", zone = "Asia/Shanghai")
	public void ingestKlines() {
		LocalDate end = LocalDate.now(CHINA_ZONE);
		List<StockBasicEntity> universe = stockBasicService.listMainBoardActive(properties.getMaxUniverseSize());
		if (universe.isEmpty()) {
			log.warn("Kline ingestion skipped: no MAIN ACTIVE stocks found");
			return;
		}
		for (String timeframe : properties.getEnabledTimeframes()) {
			int klt = properties.resolveKlt(timeframe);
			int successCount = 0;
			int totalBars = 0;
			List<String> failedSecids = new ArrayList<>();
			for (StockBasicEntity stock : universe) {
				String secid = stock.getSecid();
				try {
					int upserted = ingestionService.ingest(secid, timeframe, klt, properties.getFqt(),
							properties.getDefaultBeg(), properties.getDefaultEnd(), end, properties.getRecentLimit());
					totalBars += upserted;
					successCount++;
				} catch (Exception ex) {
					failedSecids.add(secid);
					log.error("Kline ingestion failed for secid={}, timeframe={}", secid, timeframe, ex);
				}
			}
			int failCount = failedSecids.size();
			String failList = failedSecids.stream().limit(20).collect(Collectors.joining(","));
			log.info("Kline ingestion summary: timeframe={}, totalCodes={}, success={}, fail={}, barsUpserted={}, "
							+ "failSecids={}",
					timeframe, universe.size(), successCount, failCount, totalBars, failList);
		}
	}
}
