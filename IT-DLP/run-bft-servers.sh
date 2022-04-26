#! /bin/bash

n_replicas=$1

for ((i=0; i<$n_replicas; i++)); do ./run-bft.sh $i; done

sleep 10