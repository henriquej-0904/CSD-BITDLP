#! /bin/bash

replicaId=$1

docker stop mongo-$replicaId
docker stop replica-$replicaId
