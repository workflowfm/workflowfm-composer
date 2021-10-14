# Launch the HOL Light checkpoint then load code from our project on top.
cat scripts/prover_ready.ml - | ./HOL_Light/dmtcp_restart_script.sh
