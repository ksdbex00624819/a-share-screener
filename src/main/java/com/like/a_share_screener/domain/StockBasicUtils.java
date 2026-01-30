package com.like.a_share_screener.domain;

import org.springframework.util.StringUtils;

public final class StockBasicUtils {
	private StockBasicUtils() {
	}

	public static StockBoard classifyBoard(String code) {
		if (!StringUtils.hasText(code) || code.length() < 2) {
			return StockBoard.OTHER;
		}
		if (code.startsWith("60") || code.startsWith("00")) {
			return StockBoard.MAIN;
		}
		if (code.startsWith("30")) {
			return StockBoard.CHI;
		}
		if (code.startsWith("68")) {
			return StockBoard.STAR;
		}
		if (code.startsWith("8") || code.startsWith("4")) {
			return StockBoard.BJ;
		}
		return StockBoard.OTHER;
	}

	public static String normalizeSecid(int market, String code) {
		if (!StringUtils.hasText(code)) {
			return null;
		}
		return market + "." + code;
	}
}
