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

CREATE TABLE IF NOT EXISTS stock_factor_daily (
  code VARCHAR(12) NOT NULL,
  trade_date DATE NOT NULL,
  ma5 DECIMAL(18,8),
  ma10 DECIMAL(18,8),
  ma20 DECIMAL(18,8),
  ma60 DECIMAL(18,8),
  ema5 DECIMAL(18,8),
  ema10 DECIMAL(18,8),
  ema20 DECIMAL(18,8),
  ema60 DECIMAL(18,8),
  rsi14 DECIMAL(18,8),
  macd DECIMAL(18,8),
  macd_signal DECIMAL(18,8),
  macd_hist DECIMAL(18,8),
  boll_mid DECIMAL(18,8),
  boll_up DECIMAL(18,8),
  boll_low DECIMAL(18,8),
  kdj_k DECIMAL(18,8),
  kdj_d DECIMAL(18,8),
  kdj_j DECIMAL(18,8),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (code, trade_date)
);

CREATE INDEX IF NOT EXISTS idx_stock_factor_daily_trade_date ON stock_factor_daily (trade_date);
CREATE INDEX IF NOT EXISTS idx_stock_factor_daily_code ON stock_factor_daily (code);
