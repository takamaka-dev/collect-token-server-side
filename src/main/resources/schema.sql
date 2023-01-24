CREATE TABLE collecttokenserverschema.tokencollected (
    wallet_address VARCHAR(44),
    challenge_id INTEGER AUTO_INCREMENT,
    solution_string TEXT,
    sent_challenge TEXT,
    PRIMARY KEY(wallet_address, challenge_id)
);

CREATE TABLE collecttokenserverschema.pay_sent (
    wallet_address VARCHAR(44) primary KEY,
    number_of_pay INTEGER NOT NULL DEFAULT 0
);

-- il n_shards_requested sarà: 15 * n_pay * 2
-- se wallet address not exists in collecttokenserverschema.pay_sent then n_shards_requested = 15

ALTER TABLE IF EXISTS collecttokenserverschema.tokencollected
    OWNER to collecttokenserveruser;