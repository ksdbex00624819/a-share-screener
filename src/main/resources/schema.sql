CREATE TABLE IF NOT EXISTS stock_kline (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(12) NOT NULL,
  timeframe VARCHAR(8) NOT NULL,
  bar_time DATETIME NOT NULL,
  fqt INT NOT NULL DEFAULT 1,
  open DECIMAL(18,8),
  high DECIMAL(18,8),
  low  DECIMAL(18,8),
  close DECIMAL(18,8),
  volume BIGINT,
  amount DECIMAL(20,2),
  amplitude_pct DECIMAL(10,4),
  change_pct DECIMAL(10,4),
  change_amt DECIMAL(18,8),
  turnover_pct DECIMAL(10,4),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (code, timeframe, bar_time, fqt)
);

CREATE INDEX IF NOT EXISTS idx_stock_kline_code_tf_time ON stock_kline (code, timeframe, bar_time);

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

CREATE TABLE IF NOT EXISTS stock_factor (
  code VARCHAR(12) NOT NULL,
  timeframe VARCHAR(8) NOT NULL,
  bar_time DATETIME NOT NULL,
  fqt INT NOT NULL DEFAULT 1,
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
  vol_ma5 DECIMAL(18,8),
  vol_ma10 DECIMAL(18,8),
  vol_ma20 DECIMAL(18,8),
  vol_ma60 DECIMAL(18,8),
  amt_ma20 DECIMAL(18,8),
  vol_ratio20 DECIMAL(18,8),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (code, timeframe, bar_time, fqt)
);

CREATE INDEX IF NOT EXISTS idx_stock_factor_code_tf_time ON stock_factor (code, timeframe, bar_time);
