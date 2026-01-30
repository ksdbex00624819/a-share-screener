package com.like.a_share_screener.job;

import com.like.a_share_screener.persistence.entity.StockBasicEntity;
import com.like.a_share_screener.service.FactorComputeService;
import com.like.a_share_screener.service.FactorComputationProperties;
import com.like.a_share_screener.service.StockBasicService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	@Scheduled(cron = "${factor-computation.cron:0 0 16 * * MON-FRI}", zone = "Asia/Shanghai")
	public void computeDailyFactors() {
		List<StockBasicEntity> universe = stockBasicService.listMainBoardActive(properties.getMaxUniverseSize());
		if (universe.isEmpty()) {
			log.warn("Factor computation skipped: no MAIN ACTIVE stocks found");
			return;
		}
		int computedRows = 0;
		int skippedCodes = 0;
		List<String> failedCodes = new ArrayList<>();
		for (StockBasicEntity stock : universe) {
			String code = stock.getSecid();
			try {
				int computedForCode = computeService.computeFactorsForCode(code);
				if (computedForCode == 0) {
					skippedCodes++;
				} else {
					computedRows += computedForCode;
				}
			} catch (Exception ex) {
				failedCodes.add(code);
				log.error("Factor computation failed for code={}", code, ex);
			}
		}
		String failList = failedCodes.stream().limit(20).collect(Collectors.joining(","));
		log.info("Factor computation summary: totalCodes={}, computedRows={}, skippedCodes={}, failCount={}, failCodes={}",
				universe.size(), computedRows, skippedCodes, failedCodes.size(), failList);
	}
}
