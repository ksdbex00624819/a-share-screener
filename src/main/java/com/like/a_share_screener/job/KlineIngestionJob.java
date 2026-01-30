package com.like.a_share_screener.job;

import com.like.a_share_screener.service.KlineIngestionProperties;
import com.like.a_share_screener.service.KlineIngestionService;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KlineIngestionJob {
	private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
	private final KlineIngestionService ingestionService;
	private final KlineIngestionProperties properties;

	public KlineIngestionJob(KlineIngestionService ingestionService, KlineIngestionProperties properties) {
		this.ingestionService = ingestionService;
		this.properties = properties;
	}

	@Scheduled(cron = "${kline-ingestion.cron:0 30 15 * * MON-FRI}", zone = "Asia/Shanghai")
	public void ingestDaily() {
		LocalDate end = LocalDate.now(CHINA_ZONE);
		LocalDate beg = properties.parseDefaultBeg();
		for (String code : properties.getCodes()) {
			ingestionService.ingestDaily(code, properties.getFqt(), beg, end);
		}
	}
}
