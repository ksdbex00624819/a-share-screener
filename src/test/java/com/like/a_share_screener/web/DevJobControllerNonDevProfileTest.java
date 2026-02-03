package com.like.a_share_screener.web;

import com.like.a_share_screener.job.FactorComputationJob;
import com.like.a_share_screener.job.KlineIngestionJob;
import com.like.a_share_screener.job.StockBasicSyncJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DevJobControllerNonDevProfileTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private StockBasicSyncJob stockBasicSyncJob;

	@MockBean
	private KlineIngestionJob klineIngestionJob;

	@MockBean
	private FactorComputationJob factorComputationJob;

	@Test
	void endpointsAreUnavailableOutsideDevProfile() throws Exception {
		mockMvc.perform(post("/dev/jobs/stock-basic-sync"))
				.andExpect(status().isNotFound());
	}
}
