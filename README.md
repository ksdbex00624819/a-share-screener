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
