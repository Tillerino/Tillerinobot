#!/bin/sh -e
# This script is run in the Docker container

EXPORT MALLOC_ARENA_MAX=4
java -cp "/app/*:/app/dependency/*" \
	-XX:+UseSerialGC -verbose:gc \
	-XX:-TieredCompilation \
	-Xms32m -Xmx32m \
	$JAVA_OPTS \
	org.tillerino.ppaddict.chat.irc.Main