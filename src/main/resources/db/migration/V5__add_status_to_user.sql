alter table if exists rzd_monitoring.users
    add column if not exists status text;