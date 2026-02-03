package com.like.a_share_screener.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "factor.compute")
public class FactorComputationProperties {
	private int fqt = 1;
	private int maxUniverseSize = 300;
	private String cron = "0 0 16 * * MON-FRI";
	private List<String> enabledTimeframes = new ArrayList<>(List.of("1d", "60m", "1w"));
	private Map<String, Integer> seedBarsByTimeframe = new LinkedHashMap<>(Map.of(
			"1d", 300,
			"1w", 200,
			"60m", 500
	));
	private Map<String, Integer> persistBarsByTimeframe = new LinkedHashMap<>(Map.of(
			"60m", 2000,
			"1w", 0,
			"1d", 0
	));

	public int getFqt() {
		return fqt;
	}

	public void setFqt(int fqt) {
		this.fqt = fqt;
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

	public List<String> getEnabledTimeframes() {
		return enabledTimeframes;
	}

	public void setEnabledTimeframes(List<String> enabledTimeframes) {
		this.enabledTimeframes = enabledTimeframes;
	}

	public Map<String, Integer> getSeedBarsByTimeframe() {
		return seedBarsByTimeframe;
	}

	public void setSeedBarsByTimeframe(Map<String, Integer> seedBarsByTimeframe) {
		this.seedBarsByTimeframe = seedBarsByTimeframe;
	}

	public Map<String, Integer> getPersistBarsByTimeframe() {
		return persistBarsByTimeframe;
	}

	public void setPersistBarsByTimeframe(Map<String, Integer> persistBarsByTimeframe) {
		this.persistBarsByTimeframe = persistBarsByTimeframe;
	}

	public int resolveSeedBars(String timeframe) {
		return seedBarsByTimeframe.getOrDefault(timeframe, 300);
	}

	public int resolvePersistBars(String timeframe) {
		if (persistBarsByTimeframe == null) {
			return 0;
		}
		return persistBarsByTimeframe.getOrDefault(timeframe, 0);
	}
}
