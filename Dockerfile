FROM eclipse-temurin:21.0.8_9-jre-noble
ENV PORT=8080

RUN set -eux; \
	apt-get update;

COPY structurizr-application/target/structurizr-*.war /usr/local/structurizr.war
COPY structurizr-themes /usr/local/structurizr-themes

EXPOSE ${PORT}

WORKDIR /usr/local/structurizr

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "--enable-native-access=ALL-UNNAMED", "/usr/local/structurizr.war"]