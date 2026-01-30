package com.like.a_share_screener.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class StockBasicUtilsTest {

	@Test
	void classifyBoardByCodePrefix() {
		Assertions.assertThat(StockBasicUtils.classifyBoard("600000")).isEqualTo(StockBoard.MAIN);
		Assertions.assertThat(StockBasicUtils.classifyBoard("000001")).isEqualTo(StockBoard.MAIN);
		Assertions.assertThat(StockBasicUtils.classifyBoard("300001")).isEqualTo(StockBoard.CHI);
		Assertions.assertThat(StockBasicUtils.classifyBoard("688001")).isEqualTo(StockBoard.STAR);
		Assertions.assertThat(StockBasicUtils.classifyBoard("830001")).isEqualTo(StockBoard.BJ);
		Assertions.assertThat(StockBasicUtils.classifyBoard("430001")).isEqualTo(StockBoard.BJ);
		Assertions.assertThat(StockBasicUtils.classifyBoard("200001")).isEqualTo(StockBoard.OTHER);
	}

	@Test
	void normalizeSecidUsesMarketAndCode() {
		Assertions.assertThat(StockBasicUtils.normalizeSecid(0, "000001")).isEqualTo("0.000001");
		Assertions.assertThat(StockBasicUtils.normalizeSecid(1, "600000")).isEqualTo("1.600000");
	}
}
