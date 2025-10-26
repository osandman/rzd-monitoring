DROP FUNCTION IF EXISTS f_create_db(text);
CREATE OR REPLACE FUNCTION f_create_db(dbname text)
    RETURNS void AS
$func$
DECLARE
    _host     TEXT := '127.0.0.1';
    _user     TEXT := ${POSTGRES_USER};
    _password TEXT := ${POSTGRES_PASSWORD};
BEGIN
    IF EXISTS (SELECT 1 FROM pg_database WHERE datname = dbname) THEN
        RAISE NOTICE 'Database already exists: %', dbname;
    ELSE
        -- Используем dblink для выполнения вне транзакции
        PERFORM dblink_connect('host=' || _host || ' user=' || _user || ' password=' || _password ||
                               ' dbname=' || current_database());
        PERFORM dblink_exec('CREATE DATABASE ' || dbname);
        RAISE NOTICE 'Database created: %', dbname;
    END IF;
END
$func$ LANGUAGE plpgsql;

DO
$$
    DECLARE
        db_name TEXT := 'rzd_db';
    BEGIN
        CREATE EXTENSION IF NOT EXISTS dblink; -- enable extension for dblink
        PERFORM f_create_db(db_name);
    END
$$;