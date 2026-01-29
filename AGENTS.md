# Project rules
- Tech stack: Spring Boot + MyBatis-Plus + MySQL (prod), H2 or Testcontainers (tests)
- Use Maven. Commands must pass: mvn test
- No secrets committed. Use env vars.
- Implement in small PRs, each PR has clear scope and README updates.
- Provide unit tests for parsing, upsert, incremental logic.
