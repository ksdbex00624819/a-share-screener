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
  UNIQUE KEY uk_secid (secid),
  UNIQUE KEY uk_code_market (code, market),
  INDEX idx_board_status (board, status)
);
