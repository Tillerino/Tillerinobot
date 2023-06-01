let external = ./impl.dhall

let def = (./language.dhall external.stateType)

let language : def.language = external.language
let initialState : def.initialState = external.initialState

in { }