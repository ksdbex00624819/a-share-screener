package com.like.a_share_screener.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EastmoneyRetryExecutorTest {

	@Test
	void retriesTransientExceptionsThenSucceeds() {
		EastmoneyRequestProperties properties = new EastmoneyRequestProperties();
		properties.setMaxRetries(2);
		properties.setBackoffBaseMs(100);
		properties.setBackoffMaxMs(1000);
		properties.setJitterMs(0);
		FakeSleeper sleeper = new FakeSleeper();
		EastmoneyRetryExecutor executor = new EastmoneyRetryExecutor(properties, sleeper);

		AtomicInteger attempts = new AtomicInteger();
		String result = executor.execute("op", () -> {
			if (attempts.getAndIncrement() < 2) {
				throw new EastmoneyTransientException("rc=102", 102);
			}
			return "ok";
		}, ex -> ex instanceof EastmoneyTransientException);

		Assertions.assertThat(result).isEqualTo("ok");
		Assertions.assertThat(attempts.get()).isEqualTo(3);
		Assertions.assertThat(sleeper.sleepCalls()).containsExactly(100L, 200L);
	}

	@Test
	void doesNotRetryNonRetryableException() {
		EastmoneyRequestProperties properties = new EastmoneyRequestProperties();
		properties.setMaxRetries(2);
		properties.setBackoffBaseMs(100);
		properties.setBackoffMaxMs(1000);
		properties.setJitterMs(0);
		FakeSleeper sleeper = new FakeSleeper();
		EastmoneyRetryExecutor executor = new EastmoneyRetryExecutor(properties, sleeper);

		AtomicInteger attempts = new AtomicInteger();
		Assertions.assertThatThrownBy(() -> executor.execute("op", () -> {
			attempts.incrementAndGet();
			throw new EastmoneyNonRetryableException("bad", null);
		}, ex -> ex instanceof EastmoneyTransientException))
				.isInstanceOf(EastmoneyNonRetryableException.class);

		Assertions.assertThat(attempts.get()).isEqualTo(1);
		Assertions.assertThat(sleeper.sleepCalls()).isEmpty();
	}

	@Test
	void retriesOnRc102() {
		EastmoneyRequestProperties properties = new EastmoneyRequestProperties();
		properties.setMaxRetries(1);
		properties.setBackoffBaseMs(200);
		properties.setBackoffMaxMs(1000);
		properties.setJitterMs(0);
		FakeSleeper sleeper = new FakeSleeper();
		EastmoneyRetryExecutor executor = new EastmoneyRetryExecutor(properties, sleeper);

		AtomicInteger attempts = new AtomicInteger();
		Assertions.assertThatThrownBy(() -> executor.execute("op", () -> {
			attempts.incrementAndGet();
			throw new EastmoneyTransientException("rc=102", 102);
		}, ex -> ex instanceof EastmoneyTransientException))
				.isInstanceOf(EastmoneyTransientException.class);

		Assertions.assertThat(attempts.get()).isEqualTo(2);
		Assertions.assertThat(sleeper.sleepCalls()).containsExactly(200L);
	}

	private static class FakeSleeper implements Sleeper {
		private final List<Long> calls = new ArrayList<>();

		@Override
		public void sleepMs(long ms) {
			calls.add(ms);
		}

		private List<Long> sleepCalls() {
			return calls;
		}
	}
}
