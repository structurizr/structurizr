FROM eclipse-temurin:21.0.9_10-jre-noble
ENV PORT=8080

RUN set -eux; \
	apt-get update;

COPY structurizr-application/target/structurizr-*.war /usr/local/structurizr.war
COPY structurizr-mcp/target/structurizr-*.war /usr/local/structurizr-mcp.war
COPY structurizr-themes /usr/local/structurizr-themes
COPY entrypoint.sh /usr/local/structurizr/entrypoint.sh

EXPOSE ${PORT}

WORKDIR /usr/local/structurizr

ENTRYPOINT ["/usr/local/structurizr/entrypoint.sh"]