# Structurizr

This repo represents the consolidated version of all Structurizr tooling - see [Introducing Structurizr vNext](https://www.patreon.com/posts/146923136) for more details.

## Building and running from source

Prerequisites:

- Java 21
- Maven 3.9.x

To build:

```
git clone https://github.com/structurizr/structurizr.git
cd structurizr
mvn -DexcludedGroups=IntegrationTest package
```

If any of the tests fail, you can run the following to skip the tests:

```
mvn -Dmaven.test.skip=true package
```

To run Structurizr:

```
java -jar structurizr-application/target/structurizr-1.0.0.war
```

## Supported commands

- playground: starts the [Structurizr playground](https://playground.structurizr.com)
- local: starts a local Structurizr server for workspace viewing/authoring (the equivalent of Structurizr Lite)
- server: starts a Structurizr server (the equivalent of the Structurizr on-premises installation)
- push: pushes a workspace to a Structurizr server
- pull: pulls a workspace from a Structurizr server
- lock: locks a workspace on a Structurizr server
- unlock: unlocks a workspace on a Structurizr server
- export: exports the views in a Structurizr workspace to various diagrams as code formats or a static HTML site
- merge: merges the layout information into a workspace
- validate: validates a workspace
- inspect: runs the inspections on a workspace
- list: lists the elements in a workspace
- version: displays the version information of the tooling
- help: displays a help message