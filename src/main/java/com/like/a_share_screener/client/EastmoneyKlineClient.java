package com.like.a_share_screener.client;

import com.like.a_share_screener.domain.Candle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EastmoneyKlineClient {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
	private final RestClient restClient;
	private final EastmoneyProperties properties;
	private final EastmoneyKlineParser parser;

	public EastmoneyKlineClient(EastmoneyProperties properties, EastmoneyKlineParser parser) {
		this.properties = properties;
		this.parser = parser;
		this.restClient = RestClient.builder()
				.baseUrl(properties.getBaseUrl())
				.defaultHeader(HttpHeaders.USER_AGENT, properties.getUserAgent())
				.defaultHeader(HttpHeaders.REFERER, properties.getReferer())
				.build();
	}

	public List<Candle> fetchDailyKlines(String secid, LocalDate beg, LocalDate end, int fqt) {
		Assert.hasText(secid, "secid is required");
		String url = UriComponentsBuilder.fromPath("/api/qt/stock/kline/get")
				.queryParam("secid", secid)
				.queryParam("beg", beg.format(DATE_FORMAT))
				.queryParam("end", end.format(DATE_FORMAT))
				.queryParam("klt", 101)
				.queryParam("fqt", fqt)
				.queryParam("fields1", properties.getFields1())
				.queryParam("fields2", properties.getFields2())
				.queryParam("ut", properties.getUt())
				.build()
				.toUriString();
		String response = restClient.get()
				.uri(url)
				.retrieve()
				.body(String.class);
		if (response == null || response.isBlank()) {
			return List.of();
		}
		return parser.parse(response);
	}
}
