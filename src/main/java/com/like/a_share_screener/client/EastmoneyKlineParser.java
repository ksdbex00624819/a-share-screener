package com.like.a_share_screener.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.like.a_share_screener.client.dto.EastmoneyKlineResponse;
import com.like.a_share_screener.domain.Candle;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EastmoneyKlineParser {
	private static final DateTimeFormatter DATE_TIME_SECONDS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter DATE_TIME_MINUTES_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final ObjectMapper objectMapper;

	public EastmoneyKlineParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public EastmoneyKlineResponse parseResponse(String json) {
		try {
			return objectMapper.readValue(json, EastmoneyKlineResponse.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to parse Eastmoney kline response", e);
		}
	}

	public List<Candle> parse(String json) {
		EastmoneyKlineResponse response = parseResponse(json);
		if (response == null || response.data() == null || response.data().klines() == null) {
			return Collections.emptyList();
		}
		List<Candle> candles = new ArrayList<>();
		for (String line : response.data().klines()) {
			candles.add(parseKlineLine(line));
		}
		return candles;
	}

	public Candle parseKlineLine(String line) {
		String[] parts = line.split(",");
		if (parts.length < 11) {
			throw new IllegalArgumentException("Unexpected kline line: " + line);
		}
		String timePart = parts[0];
		ParsedTime parsed = parseTimePart(timePart);
		return new Candle(
				parsed.barTime(),
				parsed.hasTime(),
				toBigDecimal(parts[1]),
				toBigDecimal(parts[3]),
				toBigDecimal(parts[4]),
				toBigDecimal(parts[2]),
				toLong(parts[5]),
				toBigDecimal(parts[6]),
				toBigDecimal(parts[7]),
				toBigDecimal(parts[8]),
				toBigDecimal(parts[9]),
				toBigDecimal(parts[10])
		);
	}

	private ParsedTime parseTimePart(String timePart) {
		LocalDateTime dateTime = tryParseDateTime(timePart, DATE_TIME_SECONDS_FORMAT);
		if (dateTime != null) {
			return new ParsedTime(dateTime, true);
		}
		dateTime = tryParseDateTime(timePart, DATE_TIME_MINUTES_FORMAT);
		if (dateTime != null) {
			return new ParsedTime(dateTime, true);
		}
		LocalDate date = tryParseDate(timePart, DATE_FORMAT);
		if (date != null) {
			return new ParsedTime(date.atStartOfDay(), false);
		}
		throw new IllegalArgumentException("Unexpected kline time: " + timePart);
	}

	private LocalDateTime tryParseDateTime(String value, DateTimeFormatter formatter) {
		try {
			return LocalDateTime.parse(value, formatter);
		} catch (RuntimeException ex) {
			return null;
		}
	}

	private LocalDate tryParseDate(String value, DateTimeFormatter formatter) {
		try {
			return LocalDate.parse(value, formatter);
		} catch (RuntimeException ex) {
			return null;
		}
	}

	private record ParsedTime(LocalDateTime barTime, boolean hasTime) {
	}

	private BigDecimal toBigDecimal(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return new BigDecimal(value);
	}

	private Long toLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return Long.parseLong(value);
	}
}
