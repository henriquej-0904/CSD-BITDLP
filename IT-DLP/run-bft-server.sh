#! /bin/bash

docker run --rm -it --network host csd-wa1-it-dlp-55065-52464 -Dlogback.configurationFile="./config/logback.xml" itdlp.tp1.impl.srv.BFTSMaRtServer $*