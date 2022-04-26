#! /bin/bash

n_replicas=$1

for ((i=0; i<$n_replicas; i++)); do docker kill "mongo-$i"; done
