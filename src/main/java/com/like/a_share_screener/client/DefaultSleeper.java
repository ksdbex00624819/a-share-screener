package com.like.a_share_screener.client;

import org.springframework.stereotype.Component;

@Component
public class DefaultSleeper implements Sleeper {
	@Override
	public void sleepMs(long ms) throws InterruptedException {
		Thread.sleep(ms);
	}
}
