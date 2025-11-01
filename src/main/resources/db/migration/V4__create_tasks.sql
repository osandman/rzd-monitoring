create table if not exists rzd_monitoring.tasks
(
    task_id             text primary key,
    chat_id             bigint                   not null,
    username            text,
    from_code           text                     not null,
    from_station        text                     not null,
    to_code             text                     not null,
    to_station          text                     not null,
    departure_date      text                     not null,
    train_departure_map jsonb                    not null,
    seat_filters        jsonb                    not null default '[]',
    state               text                     not null,
    interval_minutes    bigint                   not null default 10,
    created_at          timestamp with time zone not null default current_timestamp,
    updated_at          timestamp with time zone not null default current_timestamp,
    closed_at           timestamp with time zone,
    last_execution_at   timestamp with time zone,
    execution_count     bigint                            default 0,
    error_count         bigint                            default 0,
    last_error_message  text
);

create index if not exists idx_tasks_chat_id on rzd_monitoring.tasks (chat_id);
create index if not exists idx_tasks_state on rzd_monitoring.tasks (state);
create index if not exists idx_tasks_chat_state on rzd_monitoring.tasks (chat_id, state);
