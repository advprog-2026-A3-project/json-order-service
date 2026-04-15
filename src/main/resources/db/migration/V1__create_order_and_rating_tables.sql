CREATE TABLE IF NOT EXISTS order_table (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(100),
    product_name VARCHAR(255),
    titiper_user_id VARCHAR(100),
    jastiper_id VARCHAR(100),
    quantity INTEGER NOT NULL,
    shipping_address TEXT NOT NULL,
    total_price NUMERIC(19,2),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_order_status CHECK (status IN ('PENDING','PAID','PURCHASED','SHIPPED','COMPLETED','CANCELLED'))
);

CREATE TABLE IF NOT EXISTS rating (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    jastiper_id VARCHAR(100),
    product_id VARCHAR(100),
    rating_value INTEGER NOT NULL,
    review TEXT,
    rated_by_titiper BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_rating_value CHECK (rating_value BETWEEN 1 AND 5),
    CONSTRAINT fk_rating_order FOREIGN KEY (order_id) REFERENCES order_table(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_order_status ON order_table(status);
CREATE INDEX IF NOT EXISTS idx_order_titiper ON order_table(titiper_user_id);
CREATE INDEX IF NOT EXISTS idx_order_jastiper ON order_table(jastiper_id);
CREATE INDEX IF NOT EXISTS idx_rating_order_id ON rating(order_id);

