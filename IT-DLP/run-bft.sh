#! /bin/bash

replicaId=$1

server_port=`expr 8080 + $replicaId`
mongo_port=`expr 27017 + $replicaId`

docker run --name "replica-$replicaId" --rm -d --network host \
-v "$(pwd)/tls-config/replica-$replicaId:/app/tls-config/replica-$replicaId" \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
-v "$(pwd)/db-config.properties:/app/db-config.properties" \
-e MONGO_DB_CONNECTION_STRING="mongodb://root:password@localhost:$mongo_port" \
csd-wa1-it-dlp-55065-52464 -Dlogback.configurationFile="./config/logback.xml" itdlp.tp1.impl.srv.BFTSMaRtServer $replicaId $server_port