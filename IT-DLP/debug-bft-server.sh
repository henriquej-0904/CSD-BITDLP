#! /bin/bash

replicaId=$1

docker run --rm -it --network host -v "$(pwd)/tls-config/replica-$replicaId:/app/tls-config/replica-$replicaId" \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
csd-wa1-it-dlp-55065-52464 -agentlib:jdwp=transport=dt_socket,server=y,address=5555 \
-Dlogback.configurationFile="./config/logback.xml" itdlp.tp1.impl.srv.BFTSMaRtServer $*