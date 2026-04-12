-- JSON Order module schema for Supabase PostgreSQL

create table if not exists order_table (
    id bigserial primary key,
    product_id varchar(255),
    product_name varchar(255),
    titiper_user_id varchar(255),
    jastiper_id varchar(255),
    quantity integer not null check (quantity > 0),
    shipping_address varchar(255) not null,
    total_price numeric(19,2),
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp,
    cancelled_at timestamp,
    cancel_reason varchar(255),
    version bigint,
    constraint chk_order_status
        check (status in ('PENDING', 'PAID', 'PURCHASED', 'SHIPPED', 'COMPLETED', 'CANCELLED'))
);

create table if not exists rating (
    id bigserial primary key,
    order_id bigint not null,
    jastiper_id varchar(255),
    product_id varchar(255),
    rating_value integer not null check (rating_value between 1 and 5),
    review text,
    rated_by_titiper boolean not null default true,
    created_at timestamp not null,
    constraint fk_rating_order
        foreign key (order_id) references order_table(id) on delete cascade,
    constraint uq_rating_order unique (order_id)
);

create index if not exists idx_order_titiper on order_table(titiper_user_id);
create index if not exists idx_order_jastiper on order_table(jastiper_id);
create index if not exists idx_order_status on order_table(status);
create index if not exists idx_rating_jastiper on rating(jastiper_id);
create index if not exists idx_rating_product on rating(product_id);

