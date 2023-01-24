CREATE TABLE collecttokenserverschema.tokencollected (
wallet_address VARCHAR(44) PRIMARY KEY


);

ALTER TABLE IF EXISTS collecttokenserverschema.tokencollected
    OWNER to collecttokenserveruser;