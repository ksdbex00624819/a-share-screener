package com.like.a_share_screener.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kline.ingestion")
public class KlineIngestionProperties {
	private int fqt = 1;
	private String defaultBeg = "0";
	private String defaultEnd = "20500101";
	private String cron = "0 30 15 * * MON-FRI";
	private int maxUniverseSize = 300;
	private int recentLimit = 3000;
	private List<String> enabledTimeframes = new ArrayList<>(List.of("1d"));
	private Map<String, Integer> kltMapping = new LinkedHashMap<>(Map.of(
			"1d", 101,
			"1w", 102,
			"60m", 60
	));

	public int getFqt() {
		return fqt;
	}

	public void setFqt(int fqt) {
		this.fqt = fqt;
	}

	public String getDefaultBeg() {
		return defaultBeg;
	}

	public void setDefaultBeg(String defaultBeg) {
		this.defaultBeg = defaultBeg;
	}

	public String getDefaultEnd() {
		return defaultEnd;
	}

	public void setDefaultEnd(String defaultEnd) {
		this.defaultEnd = defaultEnd;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public int getMaxUniverseSize() {
		return maxUniverseSize;
	}

	public void setMaxUniverseSize(int maxUniverseSize) {
		this.maxUniverseSize = maxUniverseSize;
	}

	public int getRecentLimit() {
		return recentLimit;
	}

	public void setRecentLimit(int recentLimit) {
		this.recentLimit = recentLimit;
	}

	public List<String> getEnabledTimeframes() {
		return enabledTimeframes;
	}

	public void setEnabledTimeframes(List<String> enabledTimeframes) {
		this.enabledTimeframes = enabledTimeframes;
	}

	public Map<String, Integer> getKltMapping() {
		return kltMapping;
	}

	public void setKltMapping(Map<String, Integer> kltMapping) {
		this.kltMapping = kltMapping;
	}

	public int resolveKlt(String timeframe) {
		Integer value = kltMapping.get(timeframe);
		if (value == null) {
			throw new IllegalArgumentException("Missing klt mapping for timeframe " + timeframe);
		}
		return value;
	}
}
