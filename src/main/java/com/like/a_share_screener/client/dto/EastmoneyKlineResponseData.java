package com.like.a_share_screener.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EastmoneyKlineResponseData(List<String> klines) {
}
