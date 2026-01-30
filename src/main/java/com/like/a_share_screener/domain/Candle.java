package com.like.a_share_screener.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Candle(
		LocalDateTime barTime,
		boolean hasTime,
		BigDecimal open,
		BigDecimal high,
		BigDecimal low,
		BigDecimal close,
		Long volume,
		BigDecimal amount,
		BigDecimal amplitudePct,
		BigDecimal changePct,
		BigDecimal changeAmt,
		BigDecimal turnoverPct
) {
}
