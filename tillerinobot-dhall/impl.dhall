let S = { counter: Natural }
let defaults = { counter = 0 }

let decl = (./language.dhall) S

let initialState : decl.initialState = \(entropy: Integer) -> { counter = 0 }

let hello : decl.languageTypes.hello = \(state: S) -> { state = state }

let language : decl.language = { hello = hello }

in { stateType = S, initialState = initialState, language = language }