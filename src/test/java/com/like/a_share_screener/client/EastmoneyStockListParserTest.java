package com.like.a_share_screener.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.like.a_share_screener.client.dto.EastmoneyStockListResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EastmoneyStockListParserTest {

	@Test
	void parseResponseExtractsFields() {
		EastmoneyStockListParser parser = new EastmoneyStockListParser(new ObjectMapper());
		String json = """
				{
				  "rc": 0,
				  "data": {
				    "diff": [
				      {"f12": "000001", "f14": "Ping An", "f13": 0},
				      {"f12": "600000", "f14": "Shanghai Bank", "f13": 1}
				    ]
				  }
				}
				""";

		EastmoneyStockListResponse response = parser.parseResponse(json);

		Assertions.assertThat(response).isNotNull();
		Assertions.assertThat(response.rc()).isEqualTo(0);
		Assertions.assertThat(response.data().diff()).hasSize(2);
		Assertions.assertThat(response.data().diff().get(0).code()).isEqualTo("000001");
		Assertions.assertThat(response.data().diff().get(0).name()).isEqualTo("Ping An");
		Assertions.assertThat(response.data().diff().get(0).market()).isEqualTo(0);
	}
}
