#! /bin/bash

replicaId=$1

server_port=`expr 10800 + $replicaId`
mongo_port=`expr 10900 + $replicaId`

docker run --rm -it --network host \
-v "$(pwd)/tls-config/replica-$replicaId:/app/tls-config/replica-$replicaId" \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
-v "$(pwd)/server-config.properties:/app/server-config.properties" \
-v "$(pwd)/smart-contracts:/app/smart-contracts" \
-v "$(pwd)/bft-smart/config:/app/config" \
-e MONGO_DB_CONNECTION_STRING="mongodb://root:password@localhost:$mongo_port" \
csd-tp2-blockchain-it-dlp-55065-52464 -agentlib:jdwp=transport=dt_socket,server=y,address=5555 \
-Dlogback.configurationFile="./config/logback.xml" tp2.bitdlp.impl.srv.BFTSMaRtServer $replicaId $server_port