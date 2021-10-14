#!/usr/bin/env bash
while true; do
    netcat -l -p $1 -e scripts/launch_prover_docker.sh;
done