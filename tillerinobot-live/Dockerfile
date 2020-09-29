FROM adoptopenjdk/openjdk14:alpine-jre

RUN mkdir /cp

ADD target/dependency/*.jar /cp/
ADD target/live.jar /cp/live.jar

# -XX:-TieredCompilation switches off tiered compilation. While this _sounds_ bad, it's actually quite handy for us.
# It simply means that there are no intermediate compilations. We _skip_ the intermediate tiers.
# The result is far less code cache usage and fewer compiler threads :)

# For debugging the memory usage, you can try the option -XX:NativeMemoryTracking=summary (use the non-alpine image)
# and then run the command docker exec $CONTAINER_ID jcmd $PID VM.native_memory summary

CMD java -XX:+PrintFlagsFinal -XX:+UseSerialGC -verbose:gc -XX:-TieredCompilation -XX:MetaspaceSize=22M -XX:MaxMetaspaceSize=22M -XX:CompressedClassSpaceSize=3M -Xms32M -Xmx32M -Xss256k -Djdk.nio.maxCachedBufferSize=0 -cp "/cp/*" org.tillerino.ppaddict.live.LiveMain

EXPOSE 8080
