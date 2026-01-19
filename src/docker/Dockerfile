FROM azul/zulu-openjdk-alpine:21-jre-latest
COPY entrypoint.sh entrypoint.sh
ARG jar_file
COPY $jar_file keycloakmigration.jar
ENTRYPOINT ["/bin/sh","./entrypoint.sh"]
