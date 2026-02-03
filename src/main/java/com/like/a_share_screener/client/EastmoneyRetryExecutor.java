package com.like.a_share_screener.client;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

@Component
public class EastmoneyRetryExecutor {
	public interface RetryObserver {
		void onFailure(String opName, int attempt, int maxAttempts, Throwable error, long nextBackoffMs);
	}

	private final EastmoneyRequestProperties properties;
	private final Sleeper sleeper;

	public EastmoneyRetryExecutor(EastmoneyRequestProperties properties, Sleeper sleeper) {
		this.properties = properties;
		this.sleeper = sleeper;
	}

	public <T> T execute(String opName, Callable<T> action, Predicate<Throwable> shouldRetry) {
		return execute(opName, action, shouldRetry, null);
	}

	public <T> T execute(String opName, Callable<T> action, Predicate<Throwable> shouldRetry,
			RetryObserver observer) {
		Objects.requireNonNull(action, "action");
		Objects.requireNonNull(shouldRetry, "shouldRetry");
		int maxRetries = properties.getMaxRetries();
		int maxAttempts = maxRetries + 1;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			try {
				return action.call();
			} catch (Exception ex) {
				boolean retryable = shouldRetry.test(ex);
				boolean willRetry = retryable && attempt < maxRetries;
				long nextBackoffMs = willRetry ? computeBackoffMs(attempt + 1) : 0L;
				if (observer != null) {
					observer.onFailure(opName, attempt + 1, maxAttempts, ex, nextBackoffMs);
				}
				if (!willRetry) {
					throw propagate(ex);
				}
				sleepMs(nextBackoffMs);
			}
		}
		throw new IllegalStateException("Failed to execute operation " + opName);
	}

	private long computeBackoffMs(int retryAttempt) {
		long base = properties.getBackoffBaseMs();
		long max = properties.getBackoffMaxMs();
		long exponential = base * (1L << Math.max(retryAttempt - 1, 0));
		long capped = Math.min(max, exponential);
		long jitterMs = properties.getJitterMs();
		long jitter = jitterMs > 0 ? ThreadLocalRandom.current().nextLong(0, jitterMs + 1) : 0L;
		return Math.min(max, capped + jitter);
	}

	private void sleepMs(long ms) {
		try {
			sleeper.sleepMs(ms);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while backing off Eastmoney request", ex);
		}
	}

	private RuntimeException propagate(Exception ex) {
		if (ex instanceof RuntimeException runtimeException) {
			return runtimeException;
		}
		return new IllegalStateException(ex.getMessage(), ex);
	}
}
