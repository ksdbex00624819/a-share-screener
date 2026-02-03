MERGE INTO stock_basic (code, name, market, secid, board, status, created_at, updated_at)
KEY (secid)
VALUES ('000001', '平安银行', 0, '0.000001', 'MAIN', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
