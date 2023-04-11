#!/bin/sh -e
cd $(dirname "$0")
sha256sum Dockerfile $(find target -name "*.jar" | sort | grep -v "tests.jar")  | sha256sum | head -c 64
