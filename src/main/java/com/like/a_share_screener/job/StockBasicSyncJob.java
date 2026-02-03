package com.like.a_share_screener.job;

import com.like.a_share_screener.client.EastmoneyStockListClient;
import com.like.a_share_screener.client.dto.EastmoneyStockListItem;
import com.like.a_share_screener.domain.StockBasicUtils;
import com.like.a_share_screener.domain.StockStatus;
import com.like.a_share_screener.persistence.entity.StockBasicEntity;
import com.like.a_share_screener.service.StockBasicService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockBasicSyncJob {
	private static final Logger log = LoggerFactory.getLogger(StockBasicSyncJob.class);
	private final EastmoneyStockListClient stockListClient;
	private final StockBasicService stockBasicService;

	public StockBasicSyncJob(EastmoneyStockListClient stockListClient, StockBasicService stockBasicService) {
		this.stockListClient = stockListClient;
		this.stockBasicService = stockBasicService;
	}

	@Scheduled(cron = "${stock-basic-sync.cron:0 10 15 * * MON-FRI}", zone = "Asia/Shanghai")
	public void syncStockBasics() {
		runOnce();
	}

	public JobRunResult runOnce() {
		Instant start = Instant.now();
		try {
			List<EastmoneyStockListItem> items = stockListClient.fetchAllStocks();
			List<StockBasicEntity> entities = mapToEntities(items);
			StockBasicService.UpsertResult result = stockBasicService.upsertStocks(entities);
			Instant finishedAt = Instant.now();
			Duration duration = Duration.between(start, finishedAt);
			log.info("StockBasicSyncJob completed: fetched={}, inserted={}, updated={}, durationMs={}",
					items.size(), result.inserted(), result.updated(), duration.toMillis());
			return JobRunResult.success(start, finishedAt, items.size(),
					result.inserted() + result.updated(), null);
		} catch (Exception ex) {
			Instant finishedAt = Instant.now();
			log.error("StockBasicSyncJob failed", ex);
			return JobRunResult.failure(start, finishedAt, ex.getMessage());
		}
	}

	private List<StockBasicEntity> mapToEntities(List<EastmoneyStockListItem> items) {
		List<StockBasicEntity> entities = new ArrayList<>();
		for (EastmoneyStockListItem item : items) {
			if (item.code() == null || item.code().isBlank()) {
				continue;
			}
			Integer market = item.market() == null ? 0 : item.market();
			String secid = StockBasicUtils.normalizeSecid(market, item.code());
			if (secid == null) {
				continue;
			}
			StockBasicEntity entity = new StockBasicEntity();
			entity.setCode(item.code());
			entity.setName(item.name());
			entity.setMarket(market);
			entity.setSecid(secid);
			entity.setBoard(StockBasicUtils.classifyBoard(item.code()).name());
			entity.setStatus(StockStatus.ACTIVE.name());
			entities.add(entity);
		}
		return entities;
	}
}
