package com.like.a_share_screener.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EastmoneyKlineResponse(EastmoneyKlineResponseData data) {
}
