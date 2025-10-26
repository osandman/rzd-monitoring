create database rzd_db;

grant all privileges on schema rzd_monitoring to osandman;
grant usage on schema rzd_monitoring to osandman;
grant create on schema rzd_monitoring to osandman;

-- Устанавливаем default schema для пользователя osandman
alter user osandman set search_path to rzd_monitoring, public;

-- Права на все будущие таблицы и последовательности
alter default privileges in schema rzd_monitoring grant all on tables to osandman;
alter default privileges in schema rzd_monitoring grant all on sequences to osandman;
