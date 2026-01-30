package com.like.a_share_screener.client;

import com.like.a_share_screener.client.dto.EastmoneyKlineResponse;
import com.like.a_share_screener.domain.Candle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EastmoneyKlineClient {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
	private static final Logger log = LoggerFactory.getLogger(EastmoneyKlineClient.class);
	private final EastmoneyRequestExecutor requestExecutor;
	private final EastmoneyProperties properties;
	private final EastmoneyKlineParser parser;

	public EastmoneyKlineClient(EastmoneyRequestExecutor requestExecutor, EastmoneyProperties properties,
			EastmoneyKlineParser parser) {
		this.requestExecutor = requestExecutor;
		this.properties = properties;
		this.parser = parser;
	}

	public List<Candle> fetchKlines(String secid, int klt, int fqt, String beg, String end, Integer limit) {
		Assert.hasText(secid, "secid is required");
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
				.path("/api/qt/stock/kline/get")
				.queryParam("secid", secid)
				.queryParam("beg", beg)
				.queryParam("end", end)
				.queryParam("klt", klt)
				.queryParam("fqt", fqt)
				.queryParam("fields1", properties.getFields1())
				.queryParam("fields2", properties.getFields2())
				.queryParam("ut", properties.getUt());
		if (limit != null) {
			builder.queryParam("lmt", limit);
		}
		String url = builder.build().toUriString();
		int maxRetries = properties.getRequest().getMaxRetries();
		for (int attempt = 0; attempt <= maxRetries; attempt++) {
			String response = requestExecutor.get(url, "kline secid=" + secid);
			if (response == null || response.isBlank()) {
				logErrorResponse(secid, beg, end, klt, fqt, null, response);
				throw new EastmoneyApiException("Empty response from Eastmoney kline API");
			}
			EastmoneyKlineResponse parsed = parser.parseResponse(response);
			Integer rc = parsed == null ? null : parsed.rc();
			if (parsed == null || parsed.data() == null || rc == null || rc != 0) {
				logErrorResponse(secid, beg, end, klt, fqt, rc, response);
				if (attempt < maxRetries) {
					log.warn("Eastmoney kline rc retrying (attempt {}/{}), secid={}, rc={}",
							attempt + 1, maxRetries, secid, rc);
					requestExecutor.sleepBackoff(attempt + 1);
					continue;
				}
				throw new EastmoneyApiException("Unexpected Eastmoney kline response rc=" + rc);
			}
			return parser.parse(response);
		}
		throw new EastmoneyApiException("Exceeded retries for Eastmoney kline API");
	}

	public List<Candle> fetchKlines(String secid, int klt, int fqt, LocalDate beg, LocalDate end, Integer limit) {
		String begValue = beg == null ? null : beg.format(DATE_FORMAT);
		String endValue = end == null ? null : end.format(DATE_FORMAT);
		return fetchKlines(secid, klt, fqt, begValue, endValue, limit);
	}

	private void logErrorResponse(String secid, String beg, String end, int klt, int fqt, Integer rc, String response) {
		String snippet = response == null ? null : response.substring(0, Math.min(response.length(), 256));
		log.error("Eastmoney kline error secid={}, beg={}, end={}, klt={}, fqt={}, rc={}, responseSnippet={}",
				secid, beg, end, klt, fqt, rc, snippet);
	}
}
