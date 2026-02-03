package com.like.a_share_screener.client;

import com.like.a_share_screener.client.dto.EastmoneyKlineResponse;
import com.like.a_share_screener.domain.Candle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EastmoneyKlineClient {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
	private static final Logger log = LoggerFactory.getLogger(EastmoneyKlineClient.class);
	private final EastmoneyRequestExecutor requestExecutor;
	private final EastmoneyProperties properties;
	private final EastmoneyRequestProperties requestProperties;
	private final EastmoneyRetryExecutor retryExecutor;
	private final EastmoneyKlineParser parser;

	public EastmoneyKlineClient(EastmoneyRequestExecutor requestExecutor, EastmoneyProperties properties,
			EastmoneyRequestProperties requestProperties, EastmoneyRetryExecutor retryExecutor,
			EastmoneyKlineParser parser) {
		this.requestExecutor = requestExecutor;
		this.properties = properties;
		this.requestProperties = requestProperties;
		this.retryExecutor = retryExecutor;
		this.parser = parser;
	}

	public List<Candle> fetchKlines(String code, String secid, String timeframe, int klt, int fqt, String beg,
			String end, Integer limit) {
		Assert.hasText(secid, "secid is required");
		Objects.requireNonNull(timeframe, "timeframe is required");
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
		Predicate<Throwable> shouldRetry = this::isRetryable;
		return retryExecutor.execute("kline_fetch", () -> {
			String response = requestExecutor.get(url, "kline secid=" + secid);
			if (response == null || response.isBlank()) {
				logErrorResponse(secid, beg, end, klt, fqt, null, response);
				throw new EastmoneyNonRetryableException("Empty response from Eastmoney kline API", null);
			}
			EastmoneyKlineResponse parsed;
			try {
				parsed = parser.parseResponse(response);
			} catch (IllegalArgumentException ex) {
				throw new EastmoneyNonRetryableException("Failed to parse Eastmoney kline response", null, ex);
			}
			Integer rc = parsed == null ? null : parsed.rc();
			if (parsed == null || parsed.data() == null || rc == null || rc != 0) {
				logErrorResponse(secid, beg, end, klt, fqt, rc, response);
				List<Integer> retryCodes = requestProperties.getRetryRcCodes();
				if (rc != null && retryCodes != null && retryCodes.contains(rc)) {
					throw new EastmoneyTransientException("Eastmoney kline rc retryable rc=" + rc, rc);
				}
				throw new EastmoneyNonRetryableException("Unexpected Eastmoney kline response rc=" + rc, rc);
			}
			return parser.parse(response);
		}, shouldRetry, (opName, attempt, maxAttempts, error, nextBackoffMs) -> {
			if (!requestProperties.isLogFailureDetail()) {
				return;
			}
			Integer rc = extractRc(error);
			Integer httpStatus = extractHttpStatus(error);
			String err = error.getMessage() == null ? error.getClass().getSimpleName()
					: error.getClass().getSimpleName() + ":" + error.getMessage();
			log.warn("kline_fetch_fail code={} secid={} timeframe={} fqt={} attempt={}/{} rc={} httpStatus={} err={} nextBackoffMs={}",
					code, secid, timeframe, fqt, attempt, maxAttempts, rc, httpStatus, err, nextBackoffMs);
		});
	}

	public List<Candle> fetchKlines(String code, String secid, String timeframe, int klt, int fqt, LocalDate beg,
			LocalDate end, Integer limit) {
		String begValue = beg == null ? null : beg.format(DATE_FORMAT);
		String endValue = end == null ? null : end.format(DATE_FORMAT);
		return fetchKlines(code, secid, timeframe, klt, fqt, begValue, endValue, limit);
	}

	private void logErrorResponse(String secid, String beg, String end, int klt, int fqt, Integer rc, String response) {
		String snippet = response == null ? null : response.substring(0, Math.min(response.length(), 256));
		log.error("Eastmoney kline error secid={}, beg={}, end={}, klt={}, fqt={}, rc={}, responseSnippet={}",
				secid, beg, end, klt, fqt, rc, snippet);
	}

	private boolean isRetryable(Throwable ex) {
		if (ex instanceof EastmoneyTransientException) {
			return true;
		}
		if (ex instanceof RestClientResponseException responseException) {
			int status = responseException.getStatusCode().value();
			return responseException.getStatusCode().is5xxServerError() || status == 429;
		}
		if (ex instanceof RestClientException restClientException) {
			Throwable cause = restClientException.getCause();
			return cause instanceof SocketTimeoutException || cause instanceof java.io.IOException;
		}
		return false;
	}

	private Integer extractRc(Throwable error) {
		if (error instanceof EastmoneyTransientException transientException) {
			return transientException.getRc();
		}
		if (error instanceof EastmoneyNonRetryableException nonRetryableException) {
			return nonRetryableException.getRc();
		}
		return null;
	}

	private Integer extractHttpStatus(Throwable error) {
		if (error instanceof RestClientResponseException responseException) {
			return responseException.getStatusCode().value();
		}
		return null;
	}
}
