let initialState = \(s: Type) -> (Integer -> s)

-- all individual handlers are listed here so that implementations can type-check each handler individually
let hello = \(s: Type) -> (s -> { state: s })

-- now put everything together
let language = \(s: Type) -> {
    hello: hello s
}

let languageTypes = \(s: Type) -> {
    hello = hello s
}

in \(s: Type) ->  {
    initialState = initialState s,
    language = language s,
    languageTypes = languageTypes s
}