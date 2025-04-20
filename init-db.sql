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

CREATE TYPE gender AS ENUM ('MALE', 'FEMALE', 'OTHER');

CREATE TABLE IF NOT EXISTS preferences (
    id SERIAL PRIMARY KEY,
    gender gender NOT NULL,
    preferred_gender gender NOT NULL,
    age SMALLINT NOT NULL
);

CREATE TABLE IF NOT EXISTS stack (
    id SERIAL PRIMARY KEY,
    users_matching_id BIGINT[]
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

CREATE TABLE IF NOT EXISTS likes (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_target_id BIGINT NOT NULL,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_like_user_target_id ON "like" (user_target_id);

ALTER TABLE "like"
    ADD CONSTRAINT fk_like_user_target FOREIGN KEY (user_target_id) REFERENCES users(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS uq_like_pair ON "like" (user_id, user_target_id);

ALTER TABLE users
    ADD CONSTRAINT fk_preferences FOREIGN KEY (preferences_id) REFERENCES preferences(id) ON DELETE CASCADE;

ALTER TABLE users
    ADD CONSTRAINT fk_stack FOREIGN KEY (stack_id) REFERENCES stack(id) ON DELETE CASCADE;

ALTER TABLE users
    ADD CONSTRAINT fk_stack_matching FOREIGN KEY (stackMatchingData_id) REFERENCES stack_matching_data(id) ON DELETE CASCADE;