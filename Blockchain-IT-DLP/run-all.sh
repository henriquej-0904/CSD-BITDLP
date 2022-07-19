#! /bin/bash

# run multiple servers in the same machine
# usage: <min_id> <max_id>

min_id=$1
max_id=$2

for (( replicaId=$min_id; replicaId <= $max_id; replicaId++ ));
do
    cp -r Blockchain-IT-DLP Blockchain-IT-DLP-$replicaId

    cd Blockchain-IT-DLP-$replicaId

    ./run-server-with-db.sh $replicaId

    cd ..
done
