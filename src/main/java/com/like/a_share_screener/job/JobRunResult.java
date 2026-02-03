package com.like.a_share_screener.job;

import java.time.Instant;

public class JobRunResult {
	private final boolean ok;
	private final Instant startedAt;
	private final Instant finishedAt;
	private final long durationMs;
	private final Integer totalSymbols;
	private final Integer okSymbols;
	private final Integer failSymbols;
	private final String errorMessage;

	private JobRunResult(boolean ok, Instant startedAt, Instant finishedAt, long durationMs,
			Integer totalSymbols, Integer okSymbols, Integer failSymbols, String errorMessage) {
		this.ok = ok;
		this.startedAt = startedAt;
		this.finishedAt = finishedAt;
		this.durationMs = durationMs;
		this.totalSymbols = totalSymbols;
		this.okSymbols = okSymbols;
		this.failSymbols = failSymbols;
		this.errorMessage = errorMessage;
	}

	public static JobRunResult success(Instant startedAt, Instant finishedAt, Integer totalSymbols,
			Integer okSymbols, Integer failSymbols) {
		long durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();
		return new JobRunResult(true, startedAt, finishedAt, durationMs, totalSymbols, okSymbols, failSymbols, null);
	}

	public static JobRunResult failure(Instant startedAt, Instant finishedAt, String errorMessage) {
		long durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();
		return new JobRunResult(false, startedAt, finishedAt, durationMs, null, null, null, errorMessage);
	}

	public boolean isOk() {
		return ok;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public Instant getFinishedAt() {
		return finishedAt;
	}

	public long getDurationMs() {
		return durationMs;
	}

	public Integer getTotalSymbols() {
		return totalSymbols;
	}

	public Integer getOkSymbols() {
		return okSymbols;
	}

	public Integer getFailSymbols() {
		return failSymbols;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
