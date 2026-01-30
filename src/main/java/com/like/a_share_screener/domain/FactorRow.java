package com.like.a_share_screener.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FactorRow(
		LocalDateTime barTime,
		BigDecimal ma5,
		BigDecimal ma10,
		BigDecimal ma20,
		BigDecimal ma60,
		BigDecimal ema5,
		BigDecimal ema10,
		BigDecimal ema20,
		BigDecimal ema60,
		BigDecimal rsi14,
		BigDecimal macd,
		BigDecimal macdSignal,
		BigDecimal macdHist,
		BigDecimal bollMid,
		BigDecimal bollUp,
		BigDecimal bollLow,
		BigDecimal kdjK,
		BigDecimal kdjD,
		BigDecimal kdjJ,
		BigDecimal volMa5,
		BigDecimal volMa10,
		BigDecimal volMa20,
		BigDecimal volMa60,
		BigDecimal amtMa20,
		BigDecimal volRatio20
) {
}
