#! /bin/bash

# Usage: <replicaId>

replicaId=$1

blockmess_port=`expr 10000 + $replicaId`
server_port=`expr 10800 + $replicaId`
mongo_port=`expr 10900 + $replicaId`

docker run --name "replica-$replicaId" --rm -d --network host \
-v "$(pwd)/tls-config/replica-$replicaId:/app/tls-config/replica-$replicaId" \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
-v "$(pwd)/server-config.properties:/app/server-config.properties" \
-v "$(pwd)/smart-contracts:/app/smart-contracts" \
-v "$(pwd)/blockmess/config:/app/config" \
-v "$(pwd)/blockmess/keys:/app/keys" \
-v "$(pwd)/blockmess/lib:/app/lib" \
-v "$(pwd)/blockmess/logs:/app/blockmess-logs" \
-e MONGO_DB_CONNECTION_STRING="mongodb://root:password@localhost:$mongo_port" \
henriquej0904/csd-tp2-blockchain-it-dlp-55065:blockmess tp2.bitdlp.impl.srv.BlockmessServer $replicaId $server_port $blockmess_port