#!/usr/bin/env bash

# Configuration.
CAMLP5=$(camlp5 -where)
SCRIPT=$(pwd)/scripts/load_prover.ml

# Launch OCaml.
cd ./hol-light/
cat - | ocaml -I $CAMLP5 camlp5o.cma -init $SCRIPT
