package com.like.a_share_screener.web;

import com.like.a_share_screener.job.FactorComputationJob;
import com.like.a_share_screener.job.JobRunOverrides;
import com.like.a_share_screener.job.JobRunResult;
import com.like.a_share_screener.job.KlineIngestionJob;
import com.like.a_share_screener.job.StockBasicSyncJob;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/dev/jobs")
@Profile("dev")
public class DevJobController {
	private static final Logger log = LoggerFactory.getLogger(DevJobController.class);
	private static final Set<String> ALLOWED_TIMEFRAMES = Set.of("1d", "1w", "60m");

	private final StockBasicSyncJob stockBasicSyncJob;
	private final KlineIngestionJob klineIngestionJob;
	private final FactorComputationJob factorComputationJob;
	private final boolean manualJobsEnabled;

	public DevJobController(StockBasicSyncJob stockBasicSyncJob, KlineIngestionJob klineIngestionJob,
			FactorComputationJob factorComputationJob,
			@Value("${dev.manual-jobs.enabled:false}") boolean manualJobsEnabled) {
		this.stockBasicSyncJob = stockBasicSyncJob;
		this.klineIngestionJob = klineIngestionJob;
		this.factorComputationJob = factorComputationJob;
		this.manualJobsEnabled = manualJobsEnabled;
	}

	@PostMapping("/stock-basic-sync")
	public DevJobResponse runStockBasicSync() {
		ensureEnabled();
		log.info("dev_manual_job_trigger job=stock-basic-sync timeframes=null maxUniverseSize=null");
		JobRunResult result = stockBasicSyncJob.runOnce();
		return DevJobResponse.from("stock-basic-sync", result, null);
	}

	@PostMapping("/kline-ingest")
	public DevJobResponse runKlineIngest(@RequestParam(value = "timeframes", required = false) String timeframes,
			@RequestParam(value = "maxUniverseSize", required = false) Integer maxUniverseSize) {
		ensureEnabled();
		List<String> timeframeList = parseTimeframes(timeframes);
		Integer maxSize = parseMaxUniverse(maxUniverseSize);
		log.info("dev_manual_job_trigger job=kline-ingest timeframes={} maxUniverseSize={}", timeframeList, maxSize);
		JobRunOverrides overrides = new JobRunOverrides(timeframeList, maxSize);
		JobRunResult result = klineIngestionJob.runOnce(overrides);
		return DevJobResponse.from("kline-ingest", result, null);
	}

	@PostMapping("/factor-compute")
	public DevJobResponse runFactorCompute(@RequestParam(value = "timeframes", required = false) String timeframes,
			@RequestParam(value = "maxUniverseSize", required = false) Integer maxUniverseSize) {
		ensureEnabled();
		List<String> timeframeList = parseTimeframes(timeframes);
		Integer maxSize = parseMaxUniverse(maxUniverseSize);
		log.info("dev_manual_job_trigger job=factor-compute timeframes={} maxUniverseSize={}", timeframeList, maxSize);
		JobRunOverrides overrides = new JobRunOverrides(timeframeList, maxSize);
		JobRunResult result = factorComputationJob.runOnce(overrides);
		return DevJobResponse.from("factor-compute", result, null);
	}

	@PostMapping("/run-e2e")
	public DevJobResponse runE2e(@RequestParam(value = "timeframes", required = false) String timeframes,
			@RequestParam(value = "maxUniverseSize", required = false) Integer maxUniverseSize) {
		ensureEnabled();
		List<String> timeframeList = parseTimeframes(timeframes);
		Integer maxSize = parseMaxUniverse(maxUniverseSize);
		log.info("dev_manual_job_trigger job=run-e2e timeframes={} maxUniverseSize={}", timeframeList, maxSize);
		Instant startedAt = Instant.now();
		List<DevJobStep> steps = new ArrayList<>();

		JobRunResult stockResult = stockBasicSyncJob.runOnce();
		steps.add(DevJobStep.from("stock-basic-sync", stockResult));
		if (!stockResult.isOk()) {
			return DevJobResponse.from("run-e2e", startedAt, Instant.now(), false, stockResult.getErrorMessage(),
					steps);
		}

		JobRunOverrides overrides = new JobRunOverrides(timeframeList, maxSize);
		JobRunResult klineResult = klineIngestionJob.runOnce(overrides);
		steps.add(DevJobStep.from("kline-ingest", klineResult));
		if (!klineResult.isOk()) {
			return DevJobResponse.from("run-e2e", startedAt, Instant.now(), false, klineResult.getErrorMessage(),
					steps);
		}

		JobRunResult factorResult = factorComputationJob.runOnce(overrides);
		steps.add(DevJobStep.from("factor-compute", factorResult));
		boolean ok = factorResult.isOk();
		String errorMessage = factorResult.getErrorMessage();
		return DevJobResponse.from("run-e2e", startedAt, Instant.now(), ok, errorMessage, steps);
	}

	private void ensureEnabled() {
		if (!manualJobsEnabled) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Manual dev jobs are disabled");
		}
	}

	private List<String> parseTimeframes(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		String[] tokens = raw.split(",");
		Set<String> result = new LinkedHashSet<>();
		for (String token : tokens) {
			String value = token.trim();
			if (!value.isEmpty()) {
				if (!ALLOWED_TIMEFRAMES.contains(value)) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							"Unknown timeframe: " + value);
				}
				result.add(value);
			}
		}
		if (result.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeframes must not be empty");
		}
		return List.copyOf(result);
	}

	private Integer parseMaxUniverse(Integer maxUniverseSize) {
		if (maxUniverseSize == null) {
			return null;
		}
		if (maxUniverseSize <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "maxUniverseSize must be positive");
		}
		return maxUniverseSize;
	}

	public static class DevJobResponse {
		private final boolean ok;
		private final String job;
		private final Instant startedAt;
		private final Instant finishedAt;
		private final long durationMs;
		private final Integer totalSymbols;
		private final Integer okSymbols;
		private final Integer failSymbols;
		private final String errorMessage;
		private final List<DevJobStep> steps;

		private DevJobResponse(boolean ok, String job, Instant startedAt, Instant finishedAt, long durationMs,
				Integer totalSymbols, Integer okSymbols, Integer failSymbols, String errorMessage,
				List<DevJobStep> steps) {
			this.ok = ok;
			this.job = job;
			this.startedAt = startedAt;
			this.finishedAt = finishedAt;
			this.durationMs = durationMs;
			this.totalSymbols = totalSymbols;
			this.okSymbols = okSymbols;
			this.failSymbols = failSymbols;
			this.errorMessage = errorMessage;
			this.steps = steps;
		}

		public static DevJobResponse from(String job, JobRunResult result, List<DevJobStep> steps) {
			return new DevJobResponse(result.isOk(), job, result.getStartedAt(), result.getFinishedAt(),
					result.getDurationMs(), result.getTotalSymbols(), result.getOkSymbols(), result.getFailSymbols(),
					result.getErrorMessage(), steps);
		}

		public static DevJobResponse from(String job, Instant startedAt, Instant finishedAt, boolean ok,
				String errorMessage, List<DevJobStep> steps) {
			long durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();
			return new DevJobResponse(ok, job, startedAt, finishedAt, durationMs, null, null, null, errorMessage,
					steps);
		}

		public boolean isOk() {
			return ok;
		}

		public String getJob() {
			return job;
		}

		public Instant getStartedAt() {
			return startedAt;
		}

		public Instant getFinishedAt() {
			return finishedAt;
		}

		public long getDurationMs() {
			return durationMs;
		}

		public Integer getTotalSymbols() {
			return totalSymbols;
		}

		public Integer getOkSymbols() {
			return okSymbols;
		}

		public Integer getFailSymbols() {
			return failSymbols;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public List<DevJobStep> getSteps() {
			return steps;
		}
	}

	public static class DevJobStep {
		private final String job;
		private final boolean ok;
		private final Instant startedAt;
		private final Instant finishedAt;
		private final long durationMs;
		private final String errorMessage;

		private DevJobStep(String job, boolean ok, Instant startedAt, Instant finishedAt, long durationMs,
				String errorMessage) {
			this.job = job;
			this.ok = ok;
			this.startedAt = startedAt;
			this.finishedAt = finishedAt;
			this.durationMs = durationMs;
			this.errorMessage = errorMessage;
		}

		public static DevJobStep from(String job, JobRunResult result) {
			return new DevJobStep(job, result.isOk(), result.getStartedAt(), result.getFinishedAt(),
					result.getDurationMs(), result.getErrorMessage());
		}

		public String getJob() {
			return job;
		}

		public boolean isOk() {
			return ok;
		}

		public Instant getStartedAt() {
			return startedAt;
		}

		public Instant getFinishedAt() {
			return finishedAt;
		}

		public long getDurationMs() {
			return durationMs;
		}

		public String getErrorMessage() {
			return errorMessage;
		}
	}
}
