#!/bin/bash

if [ "$#" -eq 0 ]; then
    java --enable-native-access=ALL-UNNAMED -jar /usr/local/structurizr.war help
    exit 1
fi

if [[ "$1" == "mcp" ]]; then
    java -Dserver.port=${PORT} --enable-native-access=ALL-UNNAMED -jar /usr/local/structurizr-mcp.war $@
else
    java -Dserver.port=${PORT} --enable-native-access=ALL-UNNAMED -jar /usr/local/structurizr.war $@
fi