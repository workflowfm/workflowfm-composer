(* WorkflowFM Server OCaml Environment *)
print_string "Loading 'topfind'...";;
#use "topfind";;
print_string "Loading 'camlp5'...";;
#require "camlp5";;
print_string "Loading 'camlp5o.cma'...";;
#load "camlp5o.cma";;
print_string "Loading 'hol'...";;
#use "hol.ml";;
print_string "Loading 'workflowfm'...";;
loads "workflowfm/make.ml";;

(* Jev: include interactive output in this init file *)
print_string "--\nProver ready.\nval it : unit = ()\n";;