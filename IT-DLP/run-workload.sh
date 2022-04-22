#! /bin/bash

docker run --rm -it --network host \
-v "$(pwd)/tls-config/truststore.pkcs12:/app/tls-config/truststore.pkcs12" \
csd-wa1-it-dlp-55065-52464 itdlp.tp1.tests.Workload "https://localhost:8080/rest" $*