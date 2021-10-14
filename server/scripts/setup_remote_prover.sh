#! /bin/bash

if [ $# -ne 2 ]
then
  echo "Usage: setup_remote_prover [user@]host /remote/absolute/path/to/launch_prover_local.sh"
  exit -1
fi

command='ssh ## "cd @@ ; ./launch_prover_local.sh"'
command1=${command//##/$1}
command2=${command1//@@/$2}

DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo '# Launch the HOL Light checkpoint from a remote location.' > $DIR/launch_prover.sh
echo '# Script automatically created by setup_remote_prover' >> $DIR/launch_prover.sh
echo "$command2" >> $DIR/launch_prover.sh

chmod a+x $DIR/launch_prover.sh
