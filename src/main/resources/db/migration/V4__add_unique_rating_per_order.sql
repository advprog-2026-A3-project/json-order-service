-- Satu order hanya boleh memiliki satu rating dari titiper.
CREATE UNIQUE INDEX IF NOT EXISTS uq_rating_order_id ON rating(order_id);

