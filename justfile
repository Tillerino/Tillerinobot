# Prints a help text and exits to catch a bare "just" invocation
help:
  @just --list

# Build a specific Rust module for x86_64-unknown-linux-musl and copy the result to the main folder.
# Go `rustup target add x86_64-unknown-linux-musl` to install the target if not present.
build-rust-for-docker module:
  cargo build --target=x86_64-unknown-linux-musl --release --manifest-path tillerinobot-{{module}}/Cargo.toml
  cp tillerinobot-{{module}}/target/x86_64-unknown-linux-musl/release/main tillerinobot-{{module}}/target/release/main

# Build all Rust modules for x86_64-unknown-linux-musl and copy the result to the main folder
build-rust-for-docker-all:
  just build-rust-for-docker live
  just build-rust-for-docker irc

# Build all Rust modules for x86_64-unknown-linux-musl and copy the result to the main folder
build-rust-for-docker-fast:
  just build-rust-for-docker live &
  just build-rust-for-docker irc
  wait

# Clean and verify while building the Rust modules explicitly
clean-verify:
  mvn clean
  just build-rust-for-docker-all
  mvn verify

# Do more stupid things, faster :sunglasses:
clean-verify-fast:
  mvn clean
  just build-rust-for-docker-fast
  mvn verify -T 2

# Install the JARs into the Maven repository without testing anything.
install:
  mvn clean install -DskipTests -Dspotbugs.skip=true -T 2

upgrade-rust module:
  cargo update --manifest-path tillerinobot-{{module}}/Cargo.toml

upgrade-rust-all:
  just upgrade-rust live
  just upgrade-rust irc

outdated-rust module:
  # install with cargo install --locked cargo-outdated
  cargo outdated --manifest-path tillerinobot-{{module}}/Cargo.toml

outdated-rust-all:
  just outdated-rust live
  just outdated-rust irc
