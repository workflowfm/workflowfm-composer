module Clltac = Clltactics(Cllpi);;

Action.add "JOIN" Clltac.JOIN_TAC;;
Action.add "TENSOR" Clltac.TENSOR_TAC;;
Action.add "WITH" Clltac.WITH_TAC;;

module Cmpsr = Composer(Cllpi);;

module Cmpsr_json = Json_composer(Cmpsr);;
module Cenv = Json_compose_environment(Cmpsr_json);;

module Piviz = Piviz_make(Cmpsr_json);;
let piviz_deploy id name =
  let res = Piviz.deploy (Cenv.get id) name in
  (json_string_result res ; res);;

(* This is used by the composer GUI to execute HOL Light commands and detect if they succeeded or not. *)
(*
let execute f =
  try
    (f(); true)
  with 
	| Failure s -> (print_string ("Exception: " ^ s ^ "\n") ; false)
	| e -> (print_string ("Unknown exception!\n") ; false);;
*)