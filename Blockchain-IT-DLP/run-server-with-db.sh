#! /bin/bash

replicaId=$1

./run-mongo-db.sh $replicaId
./run-bft.sh $replicaId
