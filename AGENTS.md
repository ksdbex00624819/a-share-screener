# A-share screener rules (Codex)

Tech stack:
- Spring Boot + MyBatis-Plus
- Maven wrapper (use ./mvnw)
- Prod DB: MySQL
- Tests: must pass in Codex cloud (do not require external MySQL)

Quality gates:
- Every PR must pass: ./mvnw test
- No secrets in repo; use env vars
- Small PRs with clear scope
- Prefer constructor injection; keep packages clean

Data source:
- Eastmoney kline endpoint: https://push2his.eastmoney.com/api/qt/stock/kline/get
- Must send headers: User-Agent, Referer; include ut param
