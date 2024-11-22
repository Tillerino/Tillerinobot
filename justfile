updates-flags := "-q '-Dmaven.version.ignore=.*\\.Beta\\d*,.*\\.BETA\\d*,.*-beta-\\d*,.*\\.android\\d*,.*-M\\d' -Dversions.outputFile=updates.txt -Dversions.outputLineWidth=1000 -P release"
mvn := "mvn"

# Prints a help text and exits to catch a bare "just" invocation
help:
  @just --list

# Clean and verify while building the Rust modules explicitly
clean-verify:
  mvn clean verify -P rust

# Do more stupid things, faster :sunglasses:
clean-verify-fast:
  mvn clean verify -T 3 -P rust

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

outdated-java:
  {{mvn}} versions:display-plugin-updates {{updates-flags}} && { grep -- "->" updates.txt */updates.txt */*/updates.txt | sed 's/\.\+/./g'; }
  {{mvn}} versions:display-property-updates {{updates-flags}} && { grep -- "->" updates.txt */updates.txt */*/updates.txt | sed 's/\.\+/./g'; }
  {{mvn}} versions:display-dependency-updates {{updates-flags}} && { grep -- "->" updates.txt */updates.txt */*/updates.txt | sed 's/\.\+/./g'; }
  rm updates.txt */updates.txt */*/updates.txt
