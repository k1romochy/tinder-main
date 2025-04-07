DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'TinderMain') THEN
        CREATE DATABASE "TinderMain";
    END IF;
END
$$;

\c TinderMain;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS preferences (
    id SERIAL PRIMARY KEY,
    sex VARCHAR(10) NOT NULL,
    age_min SMALLINT NOT NULL,
    age_max SMALLINT NOT NULL,
    user_id BIGINT UNIQUE,
    CONSTRAINT fk_user_preferences FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS stack (
    id SERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    users_matching_id BIGINT[],
    CONSTRAINT fk_user_stack FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS stack_matching_data (
    id SERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE,
    CONSTRAINT fk_user_stack_matching FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);