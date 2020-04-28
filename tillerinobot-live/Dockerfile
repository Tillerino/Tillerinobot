FROM adoptopenjdk/openjdk14:alpine-jre

RUN mkdir /cp

ADD target/dependency/*.jar /cp/
ADD target/live.jar /cp/live.jar

CMD java -XX:+PrintFlagsFinal -verbose:gc -cp "/cp/*" org.tillerino.ppaddict.live.LiveMain

EXPOSE 8080
