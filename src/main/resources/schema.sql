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
  amplitude_pct DECIMAL(10,4),
  change_pct DECIMAL(10,4),
  change_amt DECIMAL(16,4),
  turnover_pct DECIMAL(10,4),
  fqt INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (code, trade_date)
);

CREATE TABLE IF NOT EXISTS stock_basic (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(12) NOT NULL,
  name VARCHAR(64),
  market INT NOT NULL,
  secid VARCHAR(16) NOT NULL,
  board VARCHAR(16) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (secid),
  UNIQUE (code, market)
);

CREATE INDEX IF NOT EXISTS idx_stock_basic_board_status ON stock_basic (board, status);
