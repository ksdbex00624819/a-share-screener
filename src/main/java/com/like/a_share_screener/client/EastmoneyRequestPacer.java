package com.like.a_share_screener.client;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class EastmoneyRequestPacer {
	private final EastmoneyRequestProperties properties;
	private final Sleeper sleeper;
	private final AtomicLong lastRequestTime = new AtomicLong(0);
	private final Object lock = new Object();

	public EastmoneyRequestPacer(EastmoneyRequestProperties properties, Sleeper sleeper) {
		this.properties = properties;
		this.sleeper = sleeper;
	}

	public void pace() {
		synchronized (lock) {
			long minIntervalMs = properties.getMinIntervalMs();
			long now = System.currentTimeMillis();
			long last = lastRequestTime.get();
			if (minIntervalMs > 0 && last > 0) {
				long elapsed = now - last;
				if (elapsed < minIntervalMs) {
					sleepMs(minIntervalMs - elapsed);
				}
			}
			long jitterMs = properties.getJitterMs();
			if (jitterMs > 0) {
				long jitter = ThreadLocalRandom.current().nextLong(0, jitterMs + 1);
				sleepMs(jitter);
			}
			lastRequestTime.set(System.currentTimeMillis());
		}
	}

	private void sleepMs(long ms) {
		try {
			sleeper.sleepMs(ms);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while pacing Eastmoney request", ex);
		}
	}
}
