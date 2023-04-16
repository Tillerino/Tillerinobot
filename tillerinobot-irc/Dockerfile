from openjdk:17

RUN groupadd -g 10001 appuser \
	&& useradd -u 10000 -g appuser appuser \
	&& mkdir -p /app /app/dependency \
	&& chown -R appuser:appuser /app

USER appuser
WORKDIR /app
COPY --chown=appuser:appuser irc-run.sh /app/irc-run.sh
COPY --chown=appuser:appuser target/*.jar /app/
COPY --chown=appuser:appuser target/dependency/*.jar /app/dependency/

CMD [ "sh", "irc-run.sh" ]
EXPOSE 8080