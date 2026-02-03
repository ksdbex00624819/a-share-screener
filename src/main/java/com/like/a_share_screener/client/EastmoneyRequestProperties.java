package com.like.a_share_screener.client;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eastmoney.request")
public class EastmoneyRequestProperties {
	private int timeoutMs = 8000;
	private long minIntervalMs = 200;
	private long jitterMs = 150;
	private int maxRetries = 2;
	private long backoffBaseMs = 500;
	private long backoffMaxMs = 4000;
	private List<Integer> retryRcCodes = List.of(102);
	private boolean logSuccess = true;
	private int logSuccessEveryN = 50;
	private int logProgressEveryN = 50;
	private boolean logFailureDetail = true;

	public int getTimeoutMs() {
		return timeoutMs;
	}

	public void setTimeoutMs(int timeoutMs) {
		this.timeoutMs = timeoutMs;
	}

	public long getMinIntervalMs() {
		return minIntervalMs;
	}

	public void setMinIntervalMs(long minIntervalMs) {
		this.minIntervalMs = minIntervalMs;
	}

	public long getJitterMs() {
		return jitterMs;
	}

	public void setJitterMs(long jitterMs) {
		this.jitterMs = jitterMs;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public long getBackoffBaseMs() {
		return backoffBaseMs;
	}

	public void setBackoffBaseMs(long backoffBaseMs) {
		this.backoffBaseMs = backoffBaseMs;
	}

	public long getBackoffMaxMs() {
		return backoffMaxMs;
	}

	public void setBackoffMaxMs(long backoffMaxMs) {
		this.backoffMaxMs = backoffMaxMs;
	}

	public List<Integer> getRetryRcCodes() {
		return retryRcCodes;
	}

	public void setRetryRcCodes(List<Integer> retryRcCodes) {
		this.retryRcCodes = retryRcCodes;
	}

	public boolean isLogSuccess() {
		return logSuccess;
	}

	public void setLogSuccess(boolean logSuccess) {
		this.logSuccess = logSuccess;
	}

	public int getLogSuccessEveryN() {
		return logSuccessEveryN;
	}

	public void setLogSuccessEveryN(int logSuccessEveryN) {
		this.logSuccessEveryN = logSuccessEveryN;
	}

	public int getLogProgressEveryN() {
		return logProgressEveryN;
	}

	public void setLogProgressEveryN(int logProgressEveryN) {
		this.logProgressEveryN = logProgressEveryN;
	}

	public boolean isLogFailureDetail() {
		return logFailureDetail;
	}

	public void setLogFailureDetail(boolean logFailureDetail) {
		this.logFailureDetail = logFailureDetail;
	}
}
