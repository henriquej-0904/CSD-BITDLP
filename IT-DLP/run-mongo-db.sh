#! /bin/bash

n_replicas=4

for (( replicaId=0; replicaId < $n_replicas; replicaId++ ));
do
    mongo_port=`expr 27017 + $replicaId`

    docker run --rm -d -p $mongo_port:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=password \
    mongo:4.4
done