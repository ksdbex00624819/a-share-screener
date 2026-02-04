package com.like.a_share_screener.web;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.like.a_share_screener.job.FactorComputationJob;
import com.like.a_share_screener.job.KlineIngestionJob;
import com.like.a_share_screener.job.StockBasicSyncJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
		controllers = DevJobController.class,
		excludeAutoConfiguration = {
				DataSourceAutoConfiguration.class,
				MybatisPlusAutoConfiguration.class
		}
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"dev.manual-jobs.enabled=true"
})
class DevJobControllerNonDevProfileTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StockBasicSyncJob stockBasicSyncJob;

	@MockitoBean
	private KlineIngestionJob klineIngestionJob;

	@MockitoBean
	private FactorComputationJob factorComputationJob;

	@Test
	void endpointsAreUnavailableOutsideDevProfile() throws Exception {
		mockMvc.perform(post("/dev/jobs/stock-basic-sync"))
				.andExpect(status().isNotFound());
	}
}
