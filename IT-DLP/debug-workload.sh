#! /bin/bash

# usage: <replicaId> <nUsers> <nAccounts>

replicaId=$1
replicaPort=`expr 8080 + $replicaId`

docker run --rm -it --network host \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
csd-wa1-it-dlp-55065-52464 -agentlib:jdwp=transport=dt_socket,server=y,address=4444 itdlp.tp1.tests.Workload "https://localhost:$replicaPort/rest" "replica-$replicaId" $2 $3