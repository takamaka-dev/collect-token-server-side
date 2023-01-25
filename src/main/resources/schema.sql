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


-- Table: collecttokenserverschema.pay_sent

-- DROP TABLE IF EXISTS collecttokenserverschema.pay_sent;

CREATE TABLE IF NOT EXISTS collecttokenserverschema.pay_sent
(
    wallet_address character varying(44) COLLATE pg_catalog."default" NOT NULL,
    number_of_pay integer NOT NULL DEFAULT 0,
    CONSTRAINT pay_sent_pkey PRIMARY KEY (wallet_address)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS collecttokenserverschema.pay_sent
    OWNER to collecttokenserverruser;

-- Table: collecttokenserverschema.token_collected

-- DROP TABLE IF EXISTS collecttokenserverschema.token_collected;

CREATE TABLE IF NOT EXISTS collecttokenserverschema.token_collected
(
    wallet_address character varying(44) COLLATE pg_catalog."default",
    challenge_id integer NOT NULL DEFAULT nextval('collecttokenserverschema.token_collected_challenge_id_seq'::regclass),
    solution_string text COLLATE pg_catalog."default",
    sent_challenge text COLLATE pg_catalog."default",
    pay_sent boolean DEFAULT false,
    CONSTRAINT token_collected_pkey PRIMARY KEY (challenge_id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS collecttokenserverschema.token_collected
    OWNER to collecttokenserverruser;


-- View: collecttokenserverschema.solutions_per_addr

-- DROP VIEW collecttokenserverschema.solutions_per_addr;

CREATE OR REPLACE VIEW collecttokenserverschema.solutions_per_addr
 AS
 SELECT token_collected.wallet_address,
    count(token_collected.wallet_address) AS number_of_solutions
   FROM collecttokenserverschema.token_collected
  GROUP BY token_collected.wallet_address;

ALTER TABLE collecttokenserverschema.solutions_per_addr
    OWNER TO collecttokenserverruser;

-- il n_shards_requested sarà: 15 * n_pay * 2
-- se wallet address not exists in collecttokenserverschema.pay_sent then n_shards_requested = 15

ALTER TABLE collecttokenserverschema.token_collected
    OWNER to collecttokenserverruser;

ALTER TABLE collecttokenserverschema.pay_sent
    OWNER to collecttokenserverruser;