# Prints a help text and exits to catch a bare "just" invocation
help:
  @just --list

# Do everything in a Docker container
everything-docker:
  docker run --rm -it -v $PWD:/workspace -w /workspace -v /var/run/docker.sock:/var/run/docker.sock nixos/nix nix-shell --run /bin/sh #"just everything-bare"

# Do everything in a nix-shell
everything-nix:
  nix-shell --run "just everything-bare"

# Do everything without starting a dev environment
everything-bare:
  mvn clean
  just build-rust-for-docker live
  just build-rust-for-docker irc
  mvn verify

# Build a specific Rust module for x86_64-unknown-linux-musl and copy the result to the main folder
build-rust-for-docker module:
  cargo build --target=x86_64-unknown-linux-musl --release --manifest-path tillerinobot-{{module}}/Cargo.toml
  cp tillerinobot-{{module}}/target/x86_64-unknown-linux-musl/release/main tillerinobot-{{module}}/target/release/main

# Build all Rust modules for x86_64-unknown-linux-musl and copy the result to the main folder
build-rust-for-docker-all:
  just build-rust-for-docker live
  just build-rust-for-docker irc

# Do more stupid things, but faster :sunglasses:
everything-fast:
  mvn clean
  just build-rust-for-docker live &
  just build-rust-for-docker irc
  wait
  mvn verify -T 2
