package com.like.a_share_screener.client;

import com.like.a_share_screener.client.dto.EastmoneyStockListItem;
import com.like.a_share_screener.client.dto.EastmoneyStockListResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EastmoneyStockListClient {
	private static final Logger log = LoggerFactory.getLogger(EastmoneyStockListClient.class);
	private final EastmoneyRequestExecutor requestExecutor;
	private final EastmoneyProperties properties;
	private final EastmoneyStockListParser parser;

	public EastmoneyStockListClient(EastmoneyRequestExecutor requestExecutor, EastmoneyProperties properties,
			EastmoneyStockListParser parser) {
		this.requestExecutor = requestExecutor;
		this.properties = properties;
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
		int maxRetries = properties.getRequest().getMaxRetries();
		for (int attempt = 0; attempt <= maxRetries; attempt++) {
			String response = requestExecutor.get(url, "clist pn=" + pageNo);
			if (response == null || response.isBlank()) {
				throw new EastmoneyApiException("Empty response from Eastmoney stock list API");
			}
			EastmoneyStockListResponse parsed;
			try {
				parsed = parser.parseResponse(response);
			} catch (IllegalArgumentException ex) {
				log.error("Failed to parse Eastmoney stock list page={}, error={}", pageNo, ex.getMessage(), ex);
				throw new EastmoneyApiException("Failed to parse stock list page=" + pageNo, ex);
			}
			Integer rc = parsed == null ? null : parsed.rc();
			if (parsed == null || parsed.data() == null || rc == null || rc != 0) {
				logErrorResponse(pageNo, rc, response);
				if (attempt < maxRetries) {
					log.warn("Eastmoney stock list rc retrying (attempt {}/{}), page={}, rc={}",
							attempt + 1, maxRetries, pageNo, rc);
					requestExecutor.sleepBackoff(attempt + 1);
					continue;
				}
				throw new EastmoneyApiException("Unexpected Eastmoney stock list response rc=" + rc);
			}
			List<EastmoneyStockListItem> diff = parsed.data().diff();
			return diff == null ? Collections.emptyList() : diff;
		}
		throw new EastmoneyApiException("Exceeded retries for Eastmoney stock list API");
	}

	private void logErrorResponse(int pageNo, Integer rc, String response) {
		String snippet = response == null ? null : response.substring(0, Math.min(response.length(), 256));
		log.error("Eastmoney stock list error page={}, rc={}, responseSnippet={}", pageNo, rc, snippet);
	}
}
