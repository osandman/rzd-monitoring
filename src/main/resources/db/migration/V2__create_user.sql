create table if not exists rzd_monitoring.users
(
    chat_id    bigserial primary key,
    username   text,
    first_name text,
    last_name  text,
    is_active  boolean                  default true,
    created_at timestamp with time zone default now() not null,
    updated_at timestamp with time zone default now() not null
);

create index if not exists idx_users_chat_id on rzd_monitoring.users (chat_id);