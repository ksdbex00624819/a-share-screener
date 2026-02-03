package com.like.a_share_screener.client;

import com.like.a_share_screener.client.dto.EastmoneyStockListItem;
import com.like.a_share_screener.client.dto.EastmoneyStockListResponse;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EastmoneyStockListClient {
	private static final Logger log = LoggerFactory.getLogger(EastmoneyStockListClient.class);
	private final EastmoneyRequestExecutor requestExecutor;
	private final EastmoneyProperties properties;
	private final EastmoneyRequestProperties requestProperties;
	private final EastmoneyRetryExecutor retryExecutor;
	private final EastmoneyStockListParser parser;

	public EastmoneyStockListClient(EastmoneyRequestExecutor requestExecutor, EastmoneyProperties properties,
			EastmoneyRequestProperties requestProperties, EastmoneyRetryExecutor retryExecutor,
			EastmoneyStockListParser parser) {
		this.requestExecutor = requestExecutor;
		this.properties = properties;
		this.requestProperties = requestProperties;
		this.retryExecutor = retryExecutor;
		this.parser = parser;
	}

	public List<EastmoneyStockListItem> fetchAllStocks() {
		List<EastmoneyStockListItem> all = new ArrayList<>();
		int pageNo = 1;
		while (true) {
			List<EastmoneyStockListItem> page = fetchPage(pageNo);
			if (page.isEmpty()) {
				break;
			}
			all.addAll(page);
			pageNo++;
		}
		return all;
	}

	public List<EastmoneyStockListItem> fetchPage(int pageNo) {
		String url = UriComponentsBuilder.fromUriString(properties.getClistBaseUrl())
				.path("/api/qt/clist/get")
				.queryParam("pn", pageNo)
				.queryParam("pz", properties.getClistPageSize())
				.queryParam("po", 1)
				.queryParam("np", 1)
				.queryParam("fltt", 2)
				.queryParam("invt", 2)
				.queryParam("fields", properties.getClistFields())
				.queryParam("fs", properties.getClistFs())
				.queryParam("ut", properties.getUt())
				.build()
				.toUriString();
		Predicate<Throwable> shouldRetry = this::isRetryable;
		return retryExecutor.execute("stock_list", () -> {
			String response = requestExecutor.get(url, "clist pn=" + pageNo);
			if (response == null || response.isBlank()) {
				throw new EastmoneyNonRetryableException("Empty response from Eastmoney stock list API", null);
			}
			EastmoneyStockListResponse parsed;
			try {
				parsed = parser.parseResponse(response);
			} catch (IllegalArgumentException ex) {
				log.error("Failed to parse Eastmoney stock list page={}, error={}", pageNo, ex.getMessage(), ex);
				throw new EastmoneyNonRetryableException("Failed to parse stock list page=" + pageNo, null, ex);
			}
			Integer rc = parsed == null ? null : parsed.rc();
			if (parsed == null || parsed.data() == null || rc == null || rc != 0) {
				logErrorResponse(pageNo, rc, response);
				List<Integer> retryCodes = requestProperties.getRetryRcCodes();
				if (rc != null && retryCodes != null && retryCodes.contains(rc)) {
					throw new EastmoneyTransientException("Eastmoney stock list rc retryable rc=" + rc, rc);
				}
				throw new EastmoneyNonRetryableException("Unexpected Eastmoney stock list response rc=" + rc, rc);
			}
			List<EastmoneyStockListItem> diff = parsed.data().diff();
			return diff == null ? Collections.emptyList() : diff;
		}, shouldRetry);
	}

	private void logErrorResponse(int pageNo, Integer rc, String response) {
		String snippet = response == null ? null : response.substring(0, Math.min(response.length(), 256));
		log.error("Eastmoney stock list error page={}, rc={}, responseSnippet={}", pageNo, rc, snippet);
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
}
