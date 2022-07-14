#! /bin/bash

# usage: <workload-config> <replicaId> <OPTIONAL (default is false): true for execute reads>

docker run --rm -it --network host \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
-v "$(pwd)/smart-contracts:/app/smart-contracts" \
-v "$(pwd)/workload-config:/app/workload-config" \
henriquej0904/csd-tp2-blockchain-it-dlp-55065:bft-smart tp2.bitdlp.tests.WorkloadBFT $1 replica-$2 $3