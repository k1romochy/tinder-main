DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'TinderMain') THEN
        CREATE DATABASE "TinderMain";
    END IF;
END
$$;

\c TinderMain;

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS preferences (
    id SERIAL PRIMARY KEY,
    sex VARCHAR(10) NOT NULL,
    age_min SMALLINT NOT NULL,
    age_max SMALLINT NOT NULL
);

CREATE TABLE IF NOT EXISTS stack (
    id SERIAL PRIMARY KEY,
    users_matching_id BIGINT[]  -- массив ID пользователей
);

CREATE TABLE IF NOT EXISTS stack_matching_data (
    id SERIAL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255),
    name VARCHAR(255),
    password VARCHAR(255),
    point GEOMETRY(Point, 4326),
    active BOOLEAN DEFAULT TRUE,
    preferences_id BIGINT UNIQUE,
    stack_id BIGINT UNIQUE,
    stackMatchingData_id BIGINT UNIQUE
);

ALTER TABLE users
    ADD CONSTRAINT fk_preferences FOREIGN KEY (preferences_id) REFERENCES preferences(id) ON DELETE CASCADE;

ALTER TABLE users
    ADD CONSTRAINT fk_stack FOREIGN KEY (stack_id) REFERENCES stack(id) ON DELETE CASCADE;

ALTER TABLE users
    ADD CONSTRAINT fk_stack_matching FOREIGN KEY (stackMatchingData_id) REFERENCES stack_matching_data(id) ON DELETE CASCADE;