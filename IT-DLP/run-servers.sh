for ((i=0; i<4; i++)); do ./run-bft-server.sh $i `expr 8080 + $i`; done
