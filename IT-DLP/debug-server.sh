#! /bin/bash

docker run --rm -it --network host csd-wa1-it-dlp-55065-52464 -agentlib:jdwp=transport=dt_socket,server=y,address=5555 itdlp.tp1.impl.srv.CentralizedServer
