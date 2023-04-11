#!/bin/sh -e
cd $(dirname "$0")
sha256sum Dockerfile target/release/main | sha256sum | head -c 64
