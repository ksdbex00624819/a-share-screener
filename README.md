# A-Share Screener

## Stock Universe Sync (PR2)

PR2 adds a `stock_basic` universe sourced from Eastmoney (main board by default). The sync job runs
after market close (Asia/Shanghai) and can be scheduled via `stock-basic-sync.cron`:

```yaml
stock-basic-sync:
  cron: "0 10 15 * * MON-FRI"
```

Running the sync job populates `stock_basic`, which is then used as the universe for kline ingestion
(`board = MAIN`, `status = ACTIVE`).

## Daily Kline Ingestion

The scheduler runs after market close (Asia/Shanghai). Configure the ingestion options in
`application.yml`:

```yaml
kline-ingestion:
  fqt: 1
  default-beg: 20100101
  cron: "0 30 15 * * MON-FRI"
  max-universe-size: 300
```

### Running locally

1. Ensure MySQL connection properties are set (or use the provided H2 profile for tests).
2. Start the application:

```bash
./mvnw spring-boot:run
```

The scheduled job is enabled by default and will ingest klines for the MAIN board universe stored in
`stock_basic`.

## Daily Factor Computation (PR3)

The factor computation job reads recent `stock_kline_daily` bars, computes common technical
indicators, and upserts results into `stock_factor_daily`. Warmup values are stored as `NULL`
until enough history is available (no NaN/Infinity stored).

Indicators and default parameters:

- SMA: 5, 10, 20, 60
- EMA: 5, 10, 20, 60
- RSI: 14
- MACD: 12, 26, 9 (macd = EMA(12) - EMA(26), signal = EMA(macd, 9), hist = macd - signal)
- Bollinger Bands: 20, 2 (middle = SMA(20), up/low = mid ± 2 * stddev(20))
- KDJ: 9, 3, 3 (K = SMA(%K, 3), D = SMA(K, 3), J = 3*K - 2*D)

Configure the job and seed history size in `application.yml`:

```yaml
factor-computation:
  seed-bars: 300
  cron: "0 0 16 * * MON-FRI"
  max-universe-size: 300
```
