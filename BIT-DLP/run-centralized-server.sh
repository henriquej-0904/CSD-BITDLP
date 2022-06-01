#! /bin/bash

docker run --rm -it -p 8080:8080 \
-v "$(pwd)/db-config.properties:/app/db-config.properties" \
csd-wa1-it-dlp-55065-52464 itdlp.tp1.impl.srv.CentralizedServer
