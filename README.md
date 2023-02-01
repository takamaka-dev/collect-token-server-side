# collect-token-server-side
## 1. Configuration
As first step create a bash script as follow:
```
#!/bin/bash

curl --location --request POST 'http://127.0.0.1:8080/dopendingpay'
* * * * * {PATH_TO_SCRIPT}/scheduling.sh
```
then schedule this script with crontab.

### Database
Before executing the server is mandatory to prepare the database with the instruction in the .sql file inside resource folder

## 2.Difficulty and challenge
The server will serve a challenge to any client. The difficulty of mining a blockchain refers to how challenging it is to add a new block of transactions to the blockchain network. This difficulty is adjusted regularly to ensure that new blocks are added to the blockchain at a consistent rate, regardless of the total computational power of the network. A higher difficulty means that it will take more computational power to mine a new block, while a lower difficulty means that it will take less computational power. The difficulty is important for maintaining the security and stability of the blockchain network.
