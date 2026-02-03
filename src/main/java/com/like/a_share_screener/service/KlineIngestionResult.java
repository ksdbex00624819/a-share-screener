package com.like.a_share_screener.service;

import java.time.LocalDateTime;

public record KlineIngestionResult(int upserted, int fetchedBars, LocalDateTime firstBarTime,
		LocalDateTime lastBarTime) {
}
