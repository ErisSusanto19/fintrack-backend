CREATE TABLE recurring_transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    account_id UUID NOT NULL,
    category_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    cron_expression VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_execution_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_recurring_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_account_id FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_category_id FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE INDEX idx_recurring_transactions_user_id ON recurring_transactions(user_id);