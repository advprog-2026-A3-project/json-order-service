INSERT INTO order_table (
    product_id,
    product_name,
    titiper_user_id,
    jastiper_id,
    quantity,
    shipping_address,
    total_price,
    status,
    created_at,
    updated_at,
    version
)
SELECT
    'PROD-DEMO-001',
    'Demo Matcha Kit',
    'titiper-demo',
    'jastiper-demo',
    1,
    'Depok, Indonesia',
    125000.00,
    'PENDING',
    NOW(),
    NOW(),
    0
WHERE NOT EXISTS (
    SELECT 1
    FROM order_table
    WHERE product_id = 'PROD-DEMO-001'
      AND titiper_user_id = 'titiper-demo'
);

