#! /bin/bash

docker run --rm -it --network host csd-tp2-blockchain-it-dlp-55065-52464 -agentlib:jdwp=transport=dt_socket,server=y,address=5555 tp2.bitdlp.impl.srv.CentralizedServer
