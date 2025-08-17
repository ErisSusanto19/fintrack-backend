CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    category_id UUID, -- Boleh NULL untuk budget keseluruhan
    month INT NOT NULL CHECK (month >= 1 AND month <= 12),
    year INT NOT NULL,
    amount_limit NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_budgets_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budgets_category_id FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,

    CONSTRAINT uq_user_category_period UNIQUE (user_id, category_id, year, month)
);

CREATE INDEX idx_budgets_user_period ON budgets(user_id, year, month);