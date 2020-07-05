FROM adoptopenjdk/openjdk11:alpine-jre
COPY entrypoint.sh entrypoint.sh
ARG jar_file
COPY $jar_file keycloakmigration.jar
ENTRYPOINT ["/bin/sh","./entrypoint.sh"]
