CREATE TABLE IF NOT EXISTS stock_kline_daily (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(12) NOT NULL,
  trade_date DATE NOT NULL,
  open DECIMAL(16,4),
  high DECIMAL(16,4),
  low  DECIMAL(16,4),
  close DECIMAL(16,4),
  volume BIGINT,
  amount DECIMAL(20,4),
  fqt INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (code, trade_date)
);
