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

## Kline Ingestion (1d / 1w / 60m)

The scheduler runs after market close (Asia/Shanghai). Configure the ingestion options in
`application.yml` using the `kline.ingestion.*` keys:

```yaml
kline:
  ingestion:
    enabled-timeframes: ["1d", "60m", "1w"]
    klt-mapping:
      1d: 101
      1w: 102
      60m: 60
    retention-bars-by-timeframe:
      60m: 2000
      1w: 0
      1d: 0
    fqt: 1
    default-beg: "0"
    default-end: "20500101"
    recent-limit: 120
    cron: "0 30 15 * * MON-FRI"
    max-universe-size: 300
```

Notes:
- `klt-mapping` maps the Eastmoney kline interval: 1d=101, 1w=102, 60m=60.
- `retention-bars-by-timeframe` is optional; use it to keep only the most recent N intraday bars
  (0 means unlimited).

### Running locally

1. Ensure MySQL connection properties are set (or use the provided H2 profile for tests).
2. Start the application:

```bash
./mvnw spring-boot:run
```

The scheduled job is enabled by default and will ingest klines for the MAIN board universe stored in
`stock_basic`.

## Factor Computation (1d / 1w / 60m)

The factor computation job reads recent `stock_kline` bars, computes common technical indicators,
and upserts results into `stock_factor`. Warmup values are stored as `NULL` until enough history is
available (no NaN/Infinity stored).

Indicators and default parameters:

- SMA: 5, 10, 20, 60
- EMA: 5, 10, 20, 60
- RSI: 14
- ATR: 14 (Average True Range)
- MACD: 12, 26, 9 (macd = EMA(12) - EMA(26), signal = EMA(macd, 9), hist = macd - signal)
- Bollinger Bands: 20, 2 (middle = SMA(20), up/low = mid ± 2 * stddev(20))
- KDJ: 9, 3, 3 (K = SMA(%K, 3), D = SMA(K, 3), J = 3*K - 2*D)

Configure the job and seed history size in `application.yml`:

```yaml
factor:
  compute:
    enabled-timeframes: ["1d", "60m", "1w"]
    seed-bars-by-timeframe:
      1d: 300
      1w: 200
      60m: 500
    persist-bars-by-timeframe:
      60m: 2000
      1w: 0
      1d: 0
    fqt: 1
    cron: "0 0 16 * * MON-FRI"
    max-universe-size: 300
```

Seed bar recommendations: 1d=300, 60m=500, 1w=200 to stabilize indicators.
