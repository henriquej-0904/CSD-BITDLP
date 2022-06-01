#! /bin/bash

docker run --rm -it -p 8080:8080 \
-v "$(pwd)/db-config.properties:/app/db-config.properties" \
csd-tp2-blockchain-it-dlp-55065-52464 tp2.bitdlp.impl.srv.CentralizedServer
