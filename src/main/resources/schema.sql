CREATE ROLE collecttokenserverruser WITH
  LOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION;

COMMENT ON ROLE collecttokenserverruser IS 'collect token server schema role admin';

ALTER ROLE collecttokenserverruser
        PASSWORD 'collecttokenserverpassword';

CREATE DATABASE collecttokenserverdb
    WITH 
    OWNER = collecttokenserverruser
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF8'
    LC_CTYPE = 'en_US.UTF8'
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE collecttokenserverdb TO collecttokenserverruser WITH GRANT OPTION;

CREATE SCHEMA "collecttokenserverschema"
    AUTHORIZATION collecttokenserverruser;

COMMENT ON SCHEMA "collecttokenserverschema"
    IS 'takamaka.io collect token server schema collecttokenserverschema';

GRANT ALL ON SCHEMA "collecttokenserverschema" TO collecttokenserverruser WITH GRANT OPTION;


CREATE TABLE collecttokenserverschema.token_collected (
    wallet_address VARCHAR(44),
    challenge_id SERIAL not null,
    solution_string TEXT,
    sent_challenge TEXT,
    PRIMARY KEY(challenge_id)
);

CREATE TABLE collecttokenserverschema.pay_sent (
    wallet_address VARCHAR(44) PRIMARY KEY,
    number_of_pay INTEGER NOT NULL DEFAULT 0
);

CREATE VIEW collecttokenserverschema.solutions_per_addr AS
select wallet_address, count(wallet_address) as number_of_solutions
from collecttokenserverschema.token_collected group by wallet_address

-- il n_shards_requested sarà: 15 * n_pay * 2
-- se wallet address not exists in collecttokenserverschema.pay_sent then n_shards_requested = 15

ALTER TABLE collecttokenserverschema.token_collected
    OWNER to collecttokenserverruser;

ALTER TABLE collecttokenserverschema.pay_sent
    OWNER to collecttokenserverruser;