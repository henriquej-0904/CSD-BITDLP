#! /bin/bash

replicaId=$1

# Update before executing
docker pull henriquej0904/csd-tp2-blockchain-it-dlp-55065:blockmess

./run-mongo-db.sh $replicaId
./run-server.sh $replicaId
