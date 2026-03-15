-- ============================================================
-- Create tables for ISO 8583 transaction logging and web service
-- Only create them if they don't already exist
-- ============================================================

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,

    protocol VARCHAR(20),

    tx_timestamp TIMESTAMP,
    terminal VARCHAR(50),
    amount DECIMAL(12,2),
    franchise VARCHAR(50),
    transaction_type VARCHAR(50),
    mti VARCHAR(4),
    status VARCHAR(20),
    response_code VARCHAR(10),
    rrn VARCHAR(12),
    auth_code VARCHAR(10),
    bitmap_primary VARCHAR(16),
    bitmap_secondary VARCHAR(16),

    hex_request CLOB,
    hex_response CLOB,

    artificial_delay INT,
    received_at TIMESTAMP,
    constructed_at TIMESTAMP,
    processed_at TIMESTAMP,
    response_sent_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS iso8583_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    message_type VARCHAR(10) NOT NULL, -- REQUEST or RESPONSE
    field_id VARCHAR(4) NOT NULL,
    field_value CLOB,

    CONSTRAINT fk_iso8583_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_transactions_tx_timestamp
    ON transactions(tx_timestamp);

CREATE TABLE IF NOT EXISTS digital_voucher_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    message_type VARCHAR(10) NOT NULL, -- REQUEST or RESPONSE
    field_id VARCHAR(50) NOT NULL,
    field_length INT,
    field_value CLOB,

    CONSTRAINT fk_digital_voucher_transaction
    FOREIGN KEY (transaction_id)
    REFERENCES transactions(id)
    ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_digital_voucher_transaction_id
    ON digital_voucher_fields(transaction_id);

CREATE INDEX IF NOT EXISTS idx_tx_terminal_protocol_time
    ON transactions (terminal, protocol, received_at DESC);