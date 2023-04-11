from openjdk:17

RUN groupadd -g 10001 appuser \
	&& useradd -u 10000 -g appuser appuser \
	&& mkdir -p /app /app/dependency \
	&& chown -R appuser:appuser /app

USER appuser
WORKDIR /app
COPY --chown=appuser:appuser target/*.jar /app/
COPY --chown=appuser:appuser target/dependency/*.jar /app/dependency/

ENV MALLOC_ARENA_MAX 4
CMD [ "java", "-cp", "/app/*:/app/dependency/*", \
	"-XX:+UseSerialGC", "-verbose:gc", \
	"-XX:-TieredCompilation", \
	"-Xms32m", "-Xmx32m", \
	"org.tillerino.ppaddict.chat.irc.Main" ]
EXPOSE 8080