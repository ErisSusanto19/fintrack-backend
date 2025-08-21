CREATE TABLE attachments (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(255) NOT NULL,
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    file_size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_attachments_transaction_id FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
);

CREATE INDEX idx_attachments_transaction_id ON attachments(transaction_id);