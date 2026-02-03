package com.like.a_share_screener.web;

import com.like.a_share_screener.job.FactorComputationJob;
import com.like.a_share_screener.job.JobRunOverrides;
import com.like.a_share_screener.job.JobRunResult;
import com.like.a_share_screener.job.KlineIngestionJob;
import com.like.a_share_screener.job.StockBasicSyncJob;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DevJobControllerDevProfileTest {
	private static final Instant STARTED_AT = Instant.parse("2024-01-01T00:00:00Z");
	private static final Instant FINISHED_AT = Instant.parse("2024-01-01T00:00:00Z");
	private static final JobRunResult SUCCESS_RESULT =
			JobRunResult.success(STARTED_AT, FINISHED_AT, 1, 1, 0);

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private StockBasicSyncJob stockBasicSyncJob;

	@MockBean
	private KlineIngestionJob klineIngestionJob;

	@MockBean
	private FactorComputationJob factorComputationJob;

	@BeforeEach
	void setUp() {
		when(stockBasicSyncJob.runOnce()).thenReturn(SUCCESS_RESULT);
		when(klineIngestionJob.runOnce(any())).thenReturn(SUCCESS_RESULT);
		when(factorComputationJob.runOnce(any())).thenReturn(SUCCESS_RESULT);
	}

	@Test
	void stockBasicEndpointReturnsOk() throws Exception {
		mockMvc.perform(post("/dev/jobs/stock-basic-sync"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ok").value(true))
				.andExpect(jsonPath("$.job").value("stock-basic-sync"));
	}

	@Test
	void klineOverrideParsingPassesOverridesToJob() throws Exception {
		mockMvc.perform(post("/dev/jobs/kline-ingest")
						.param("timeframes", "1d,60m")
						.param("maxUniverseSize", "10"))
				.andExpect(status().isOk());

		ArgumentCaptor<JobRunOverrides> captor = ArgumentCaptor.forClass(JobRunOverrides.class);
		verify(klineIngestionJob).runOnce(captor.capture());
		JobRunOverrides overrides = captor.getValue();
		assertThat(overrides.getTimeframes()).containsExactly("1d", "60m");
		assertThat(overrides.getMaxUniverseSize()).isEqualTo(10);
	}

	@Test
	void invalidTimeframeReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/dev/jobs/kline-ingest")
						.param("timeframes", "2d"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void runE2eInvokesJobsSequentially() throws Exception {
		mockMvc.perform(post("/dev/jobs/run-e2e")
						.param("timeframes", "1d")
						.param("maxUniverseSize", "5"))
				.andExpect(status().isOk());

		InOrder inOrder = inOrder(stockBasicSyncJob, klineIngestionJob, factorComputationJob);
		inOrder.verify(stockBasicSyncJob).runOnce();
		inOrder.verify(klineIngestionJob).runOnce(any());
		inOrder.verify(factorComputationJob).runOnce(any());
	}
}
