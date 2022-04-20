#! /bin/bash

docker run --rm -it --network host csd-wa1-it-dlp-55065-52464 itdlp.tp1.tests.Workload "http://localhost:8080/rest" $*