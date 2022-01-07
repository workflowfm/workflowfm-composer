# Launch the HOL Light checkpoint then load code from our project on top.
cat scripts/prover_ready.ml - | ./hol-light/dmtcp_restart_script.sh
