package com.like.a_share_screener.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EastmoneyStockListItem(
		@JsonProperty("f12") String code,
		@JsonProperty("f14") String name,
		@JsonProperty("f13") Integer market
) {
}
