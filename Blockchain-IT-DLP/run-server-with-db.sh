#! /bin/bash

replicaId=$1

# Update before executing
docker pull henriquej0904/csd-tp2-blockchain-it-dlp-55065:bft-smart

./run-mongo-db.sh $replicaId
./run-bft.sh $replicaId
