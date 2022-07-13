#! /bin/bash

# usage: <replicaId> <fReplicas>

replicaId=$1
replicaPort=`expr 8080 + $replicaId`

docker run --rm -it --network host \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
-v "$(pwd)/smart-contracts:/app/smart-contracts" \
csd-tp2-blockchain-it-dlp-55065-52464 tp2.bitdlp.tests.WorkloadBFT "https://localhost:$replicaPort/rest" "replica-$replicaId" $2