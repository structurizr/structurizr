#!/bin/bash

if [ "$#" -eq 0 ]; then
    java --enable-native-access=ALL-UNNAMED -jar /usr/local/structurizr.war help
    exit 1
fi

java -Dserver.port=${PORT} -Dio.netty.noUnsafe=true --enable-native-access=ALL-UNNAMED -jar /usr/local/structurizr.war $@