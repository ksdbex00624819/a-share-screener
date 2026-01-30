package com.like.a_share_screener.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.like.a_share_screener.client.dto.EastmoneyStockListResponse;
import org.springframework.stereotype.Component;

@Component
public class EastmoneyStockListParser {
	private final ObjectMapper objectMapper;

	public EastmoneyStockListParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public EastmoneyStockListResponse parseResponse(String json) {
		try {
			return objectMapper.readValue(json, EastmoneyStockListResponse.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to parse Eastmoney stock list response", e);
		}
	}
}
