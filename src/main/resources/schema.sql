DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS requests CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS comments CASCADE;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    description VARCHAR(512),
    requestor_id BIGINT REFERENCES users (id),
    CONSTRAINT pk_requests PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS items(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    description VARCHAR(512) NOT NULL,
    is_available BOOLEAN NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES users(id),
    request_id BIGINT REFERENCES requests(id),
    CONSTRAINT pk_items_id PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS bookings(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    item_id BIGINT NOT NULL REFERENCES items(id),
    booker_id  BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(51),
    CONSTRAINT pk_bookings_id PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS comments(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    text VARCHAR(512) NOT NULL,
    item_id BIGINT NOT NULL REFERENCES items(id),
    author_id BIGINT NOT NULL REFERENCES users(id),
    created TIMESTAMP NOT NULL,
    CONSTRAINT pk_comments PRIMARY KEY (id)
);

