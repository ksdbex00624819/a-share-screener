package com.like.a_share_screener.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kline-ingestion")
public class KlineIngestionProperties {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
	private int fqt = 1;
	private String defaultBeg = "19900101";
	private String cron = "0 30 15 * * MON-FRI";
	private int maxUniverseSize = 300;

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

	public LocalDate parseDefaultBeg() {
		return LocalDate.parse(defaultBeg, DATE_FORMAT);
	}
}
