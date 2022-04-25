#! /bin/bash

replicaId=$1

mongo_port=`expr 27017 + $replicaId`

docker run --rm -it --network host -v "$(pwd)/tls-config/replica-$replicaId:/app/tls-config/replica-$replicaId" \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
-e MONGO_DB_CONNECTION_STRING="mongodb://root:password@localhost:$mongo_port" \
csd-wa1-it-dlp-55065-52464 -agentlib:jdwp=transport=dt_socket,server=y,address=5555 \
-Dlogback.configurationFile="./config/logback.xml" itdlp.tp1.impl.srv.BFTSMaRtServer $*