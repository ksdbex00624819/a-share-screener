package com.like.a_share_screener.job;

import com.like.a_share_screener.persistence.entity.StockBasicEntity;
import com.like.a_share_screener.service.FactorComputeService;
import com.like.a_share_screener.service.FactorComputationProperties;
import com.like.a_share_screener.service.StockBasicService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FactorComputationJob {
	private static final Logger log = LoggerFactory.getLogger(FactorComputationJob.class);
	private final FactorComputeService computeService;
	private final FactorComputationProperties properties;
	private final StockBasicService stockBasicService;

	public FactorComputationJob(FactorComputeService computeService, FactorComputationProperties properties,
			StockBasicService stockBasicService) {
		this.computeService = computeService;
		this.properties = properties;
		this.stockBasicService = stockBasicService;
	}

	@Scheduled(cron = "${factor.compute.cron:0 0 16 * * MON-FRI}", zone = "Asia/Shanghai")
	public void computeFactors() {
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
			List<StockBasicEntity> universe = stockBasicService.listMainBoardActive(maxUniverseSize);
			if (universe.isEmpty()) {
				log.warn("Factor computation skipped: no MAIN ACTIVE stocks found");
				Instant finishedAt = Instant.now();
				return JobRunResult.success(startedAt, finishedAt, 0, 0, 0);
			}
			int successTotal = 0;
			int failTotal = 0;
			for (String timeframe : timeframes) {
				int computedRows = 0;
				int skippedCodes = 0;
				List<String> failedCodes = new ArrayList<>();
				for (StockBasicEntity stock : universe) {
					String code = stock.getSecid();
					try {
						int computedForCode = computeService.computeFactorsForCode(code, timeframe);
						if (computedForCode == 0) {
							skippedCodes++;
						} else {
							computedRows += computedForCode;
							successTotal++;
						}
					} catch (Exception ex) {
						failTotal++;
						failedCodes.add(code);
						log.error("Factor computation failed for code={}, timeframe={}", code, timeframe, ex);
					}
				}
				String failList = failedCodes.stream().limit(20).collect(Collectors.joining(","));
				log.info("Factor computation summary: timeframe={}, totalCodes={}, computedRows={}, skippedCodes={}, "
								+ "failCount={}, failCodes={}",
						timeframe, universe.size(), computedRows, skippedCodes, failedCodes.size(), failList);
			}
			int totalSymbols = universe.size() * timeframes.size();
			Instant finishedAt = Instant.now();
			return JobRunResult.success(startedAt, finishedAt, totalSymbols, successTotal, failTotal);
		} catch (Exception ex) {
			Instant finishedAt = Instant.now();
			log.error("Factor computation failed", ex);
			return JobRunResult.failure(startedAt, finishedAt, ex.getMessage());
		}
	}
}
