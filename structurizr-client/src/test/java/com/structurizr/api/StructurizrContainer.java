package com.structurizr.api;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.File;

class StructurizrContainer extends GenericContainer<StructurizrContainer> {

    private static final String STRUCTURIZR_DOCKER_IMAGE = "structurizr/structurizr:preview";
    private static final String PATH_IN_CONTAINER = "/usr/local/structurizr";

    public StructurizrContainer(File structurizrDataDirectory) {
        super(DockerImageName.parse(STRUCTURIZR_DOCKER_IMAGE));

        withExposedPorts(8080);
        withFileSystemBind(structurizrDataDirectory.getAbsolutePath(), PATH_IN_CONTAINER, BindMode.READ_WRITE);
        withCommand("server");
        waitingFor(Wait.forHttp("/"));
    }

}