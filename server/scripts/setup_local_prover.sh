#! /bin/bash

DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cp $DIR/launch_prover_local.sh $DIR/launch_prover.sh

chmod a+x $DIR/launch_prover.sh
