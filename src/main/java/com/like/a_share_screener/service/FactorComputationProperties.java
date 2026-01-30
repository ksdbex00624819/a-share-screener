package com.like.a_share_screener.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "factor-computation")
public class FactorComputationProperties {
	private int seedBars = 300;
	private String cron = "0 0 16 * * MON-FRI";
	private int maxUniverseSize = 300;

	public int getSeedBars() {
		return seedBars;
	}

	public void setSeedBars(int seedBars) {
		this.seedBars = seedBars;
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
}
