#! /bin/bash

# usage: <replicaId> <nUsers> <nAccounts> <fReplicas>

replicaId=$1
replicaPort=`expr 8080 + $replicaId`

docker run --rm -it --network host \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
csd-tp2-blockchain-it-dlp-55065-52464 -agentlib:jdwp=transport=dt_socket,server=y,address=4444 tp2.bitdlp.tests.WorkloadBFT "https://localhost:$replicaPort/rest" "replica-$replicaId" $2 $3 $4