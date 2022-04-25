for ((i=0; i<4; i++)); do ./run-bft.sh $i `expr 8080 + $i`; done
