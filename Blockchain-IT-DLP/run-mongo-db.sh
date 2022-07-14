#! /bin/bash

mongo_port=`expr 10900 + $replicaId`

docker run --name "mongo-$replicaId" --rm -d -p $mongo_port:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=password \
mongo:4.4
