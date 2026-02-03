package com.like.a_share_screener.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.like.a_share_screener.domain.Candle;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EastmoneyKlineParserTest {

	@Test
	void parseKlineLine() {
		EastmoneyKlineParser parser = new EastmoneyKlineParser(new ObjectMapper());
		Candle candle = parser.parseKlineLine("2024-01-02,12.34,12.50,12.60,12.20,123456,987654.32,3.21,1.29,0.16,0.45");

		Assertions.assertThat(candle.barTime()).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0));
		Assertions.assertThat(candle.hasTime()).isFalse();
		Assertions.assertThat(candle.open()).isEqualByComparingTo(new BigDecimal("12.34"));
		Assertions.assertThat(candle.high()).isEqualByComparingTo(new BigDecimal("12.60"));
		Assertions.assertThat(candle.low()).isEqualByComparingTo(new BigDecimal("12.20"));
		Assertions.assertThat(candle.close()).isEqualByComparingTo(new BigDecimal("12.50"));
		Assertions.assertThat(candle.volume()).isEqualTo(123456L);
		Assertions.assertThat(candle.amount()).isEqualByComparingTo(new BigDecimal("987654.32"));
		Assertions.assertThat(candle.amplitudePct()).isEqualByComparingTo(new BigDecimal("3.21"));
		Assertions.assertThat(candle.changePct()).isEqualByComparingTo(new BigDecimal("1.29"));
		Assertions.assertThat(candle.changeAmt()).isEqualByComparingTo(new BigDecimal("0.16"));
		Assertions.assertThat(candle.turnoverPct()).isEqualByComparingTo(new BigDecimal("0.45"));
	}

	@Test
	void parseJsonResponse() {
		EastmoneyKlineParser parser = new EastmoneyKlineParser(new ObjectMapper());
		String json = """
				{
				  "data": {
				    "klines": [
				      "2024-01-02,12.34,12.50,12.60,12.20,123456,987654.32,3.21,1.29,0.16,0.45"
				    ]
				  }
				}
				""";

		List<Candle> candles = parser.parse(json);

		Assertions.assertThat(candles).hasSize(1);
		Assertions.assertThat(candles.get(0).barTime()).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0));
	}

	@Test
	void parseKlineLineWithTime() {
		EastmoneyKlineParser parser = new EastmoneyKlineParser(new ObjectMapper());
		Candle candle = parser.parseKlineLine(
				"2024-01-02 10:30,12.34,12.50,12.60,12.20,123456,987654.32,3.21,1.29,0.16,0.45");

		Assertions.assertThat(candle.barTime()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 30));
		Assertions.assertThat(candle.hasTime()).isTrue();
	}

	@Test
	void parseKlineLineWithSeconds() {
		EastmoneyKlineParser parser = new EastmoneyKlineParser(new ObjectMapper());
		Candle candle = parser.parseKlineLine(
				"2024-01-02 10:30:15,12.34,12.50,12.60,12.20,123456,987654.32,3.21,1.29,0.16,0.45");

		Assertions.assertThat(candle.barTime()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 30, 15));
		Assertions.assertThat(candle.hasTime()).isTrue();
	}
}
