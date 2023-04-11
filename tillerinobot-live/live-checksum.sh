#!/bin/sh -e
DIR=$(dirname "$0")
sha256sum $DIR/Dockerfile $DIR/target/release/main | sha256sum | head -c 64
