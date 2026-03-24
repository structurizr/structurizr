FROM dhi.io/eclipse-temurin:21-alpine3.23

ENV PORT=8080

COPY ./target/structurizr-*.war /usr/local/structurizr-mcp.war

EXPOSE ${PORT}

WORKDIR /usr/local/structurizr

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "/usr/local/structurizr-mcp.war"]