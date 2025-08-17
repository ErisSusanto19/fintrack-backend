ALTER TABLE transactions
ADD COLUMN transfer_id UUID;

CREATE INDEX idx_transactions_transfer_id ON transactions(transfer_id);