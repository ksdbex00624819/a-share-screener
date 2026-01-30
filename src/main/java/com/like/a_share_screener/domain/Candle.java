package com.like.a_share_screener.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Candle(
		LocalDate tradeDate,
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
