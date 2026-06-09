updates-flags := "-q '-Dmaven.version.ignore=.*\\.Beta\\d*,.*\\.BETA\\d*,.*-beta-\\d*,.*\\.android\\d*,.*-M\\d' -Dversions.outputFile=updates.txt -Dversions.outputLineWidth=1000 -P release"
# mvnd is buggy in worktrees
#mvnd := `if command -v mvnd &> /dev/null; then echo mvnd; else echo mvn; fi`
mvnd := "mvn"
skipChecks := "-DskipTests -DskipIT -Dspotless.skip=true -Dspotbugs.skip=true -Djacoco.skip=true -Denforcer.skip=true -Dimpsort.skip=true -Dmdep.analyze.skip=true"

# Prints a help text and exits to catch a bare "just" invocation
help:
  @just --list

compile:
  mvn clean spotless:apply test-compile -T 99

# Clean and verify while building the Rust modules explicitly
clean-verify:
  {{mvnd}} clean
  mvn verify -P rust

# Do more stupid things, faster :sunglasses:
clean-verify-fast:
  mvn validate # spotless check first to prevent disappointments
  {{mvnd}} clean
  mvn verify -T 3 -P rust

# Install the JARs into the Maven repository without testing anything.
install:
  mvn clean install -DskipTests -Dspotbugs.skip=true -T 2

single-ppaddict-test name:
  {{mvnd}} spotless:apply -T 99
  {{mvnd}} -Dmaven.repo.local={{justfile_directory()}}/.m2/repository clean spotless:apply install {{skipChecks}} -Dgwt.skipCompilation=true -pl :ppaddict-site -am -T 99
  {{mvnd}} -Dmaven.repo.local={{justfile_directory()}}/.m2/repository test -Dtest={{ quote(name) }} -Dgwt.skipCompilation=true -pl :ppaddict-site

test-ppaddict:
  {{mvnd}} clean spotless:apply test -pl :ppaddict-site -am -T 99

run-ppaddict port:
  {{mvnd}} spotless:apply -T 99
  {{mvnd}} clean spotless:apply test-compile dependency:build-classpath {{skipChecks}} -Dgwt.skipCompilation=true -DincludeScope=test -Dmdep.outputFile=target/cp -pl :ppaddict-site -am -T 99
  # We can't use mvnd to run, or the java process will not be a child of the just process, which we use to clean up
  cd ppaddict-site; PPADDICT_PORT={{port}} java -cp $(cat target/cp):target/classes:target/test-classes org.tillerino.ppaddict.LocalPpaddict

recycle-ppaddict:
  #!/usr/bin/env sh
  case "$(basename "$(pwd)")" in
    wt1) PORT=8081 ;;
    wt2) PORT=8082 ;;
    wt3) PORT=8083 ;;
    wt4) PORT=8084 ;;
    *)   PORT=8080 ;;
  esac

  PIDS=$(pgrep -f "just run-ppaddict $PORT")
  for PID in $PIDS; do
    echo killing $PID
    pkill -9 -P "$PID"
    kill -9 "$PID"
  done

  echo starting on $PORT
  just run-ppaddict $PORT > ppaddict-site/local.log 2>&1 &

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
  {{mvnd}} versions:display-plugin-updates {{updates-flags}} && { grep -- "->" updates.txt */updates.txt */*/updates.txt | sed 's/\.\+/./g'; }
  {{mvnd}} versions:display-property-updates {{updates-flags}} && { grep -- "->" updates.txt */updates.txt */*/updates.txt | sed 's/\.\+/./g'; }
  {{mvnd}} versions:display-dependency-updates {{updates-flags}} && { grep -- "->" updates.txt */updates.txt */*/updates.txt | sed 's/\.\+/./g'; }
  rm updates.txt */updates.txt */*/updates.txt

san-doku-build-and-run:
  docker build -t san-doku 'https://github.com/omkelderman/SanDoku.git#2025.710.0-lazer'
  docker run --rm -it -p 8080:8080 san-doku

san-doku-get-spec:
  curl -f http://localhost:8080/swagger/v1/swagger.json > tillerinobot/src/main/openapi/san-doku.json
