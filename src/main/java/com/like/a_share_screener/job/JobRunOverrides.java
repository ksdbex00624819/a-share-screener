package com.like.a_share_screener.job;

import java.util.Collections;
import java.util.List;

public class JobRunOverrides {
	private final List<String> timeframes;
	private final Integer maxUniverseSize;

	public JobRunOverrides(List<String> timeframes, Integer maxUniverseSize) {
		this.timeframes = timeframes == null ? null : Collections.unmodifiableList(timeframes);
		this.maxUniverseSize = maxUniverseSize;
	}

	public List<String> getTimeframes() {
		return timeframes;
	}

	public Integer getMaxUniverseSize() {
		return maxUniverseSize;
	}
}
