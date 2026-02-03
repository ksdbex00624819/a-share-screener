package com.like.a_share_screener.client;

import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EastmoneyRequestPacerTest {

	@Test
	void paceEnforcesMinInterval() {
		EastmoneyRequestProperties properties = new EastmoneyRequestProperties();
		properties.setMinIntervalMs(1000);
		properties.setJitterMs(0);
		FakeSleeper sleeper = new FakeSleeper();
		EastmoneyRequestPacer pacer = new EastmoneyRequestPacer(properties, sleeper);

		pacer.pace();
		pacer.pace();

		Assertions.assertThat(sleeper.sleepCalls()).hasSize(1);
		Assertions.assertThat(sleeper.sleepCalls().get(0)).isBetween(1L, 1000L);
	}

	@Test
	void paceAppliesJitterWithinRange() {
		EastmoneyRequestProperties properties = new EastmoneyRequestProperties();
		properties.setMinIntervalMs(0);
		properties.setJitterMs(150);
		FakeSleeper sleeper = new FakeSleeper();
		EastmoneyRequestPacer pacer = new EastmoneyRequestPacer(properties, sleeper);

		pacer.pace();
		pacer.pace();
		pacer.pace();

		Assertions.assertThat(sleeper.sleepCalls()).hasSize(3);
		Assertions.assertThat(sleeper.sleepCalls())
				.allSatisfy(value -> Assertions.assertThat(value).isBetween(0L, 150L));
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
