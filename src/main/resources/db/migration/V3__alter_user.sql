alter table if exists rzd_monitoring.users
    add column language_code text,
    add column is_bot        boolean,
    add column is_premium    boolean