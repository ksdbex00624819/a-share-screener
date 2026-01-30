# A-Share Screener

## Daily Kline Ingestion

The scheduler runs after market close (Asia/Shanghai). Configure the code universe and cron in
`application.yml`:

```yaml
kline-ingestion:
  codes:
    - 1.000001
    - 0.399001
  fqt: 1
  default-beg: 20100101
  cron: "0 30 15 * * MON-FRI"
```

### Running locally

1. Ensure MySQL connection properties are set (or use the provided H2 profile for tests).
2. Start the application:

```bash
./mvnw spring-boot:run
```

The scheduled job is enabled by default and will invoke the daily ingestion for each configured code.
