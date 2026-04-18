# Changelog

## v6.2.0 (18th April 2026)

### structurizr-core

- Adds support for setting deployment groups on deployment nodes, which are inherited by software system instances and container instances when added.
- Removes the default deployment group of "Default".
- Fixes https://github.com/structurizr/structurizr/issues/64 (responses cannot be added if the request is not present in the view).

### structurizr-dsl

- Throws an exception when an identifier is already in use, irrespective of whether the same element/relationship is being registered again.
- Adds support for setting the deployment group on a deployment node (https://github.com/structurizr/structurizr/issues/62).
- Adds a way to exclude files by filename or regex when using `!docs` and `!adrs` (https://github.com/structurizr/structurizr/issues/72).
- `!docs` and `!adrs` now uses the reluctant version of the `DefaultImageImporter` (see below).

### structurizr-export

- Fixes https://github.com/structurizr/structurizr/issues/61 (support multiple groups having the same name when using PlantUML).
- Adds support for software system/container boundaries on PlantUML sequence diagrams, configurable via a property named `plantuml.boundaries` (https://github.com/structurizr/structurizr/issues/63).
- Adds support for specifying `name=value` skinparam pairs via a view/viewset property named `plantuml.skinparams` (workaround for https://github.com/structurizr/structurizr/issues/75).

### structurizr-import

- Adds a `reluctant` property to the `DefaultImageImporter`, providing a way to only import images that are referenced in the documentation/decisions.

## v6.1.0 (6th March 2026)

### structurizr-core

- Normalises line endings when importing Markdown/AsciiDoc documentation and decisions (https://github.com/structurizr/structurizr/issues/22).
- Adds validation that `structurizr.groupSeparator` is a single character (https://github.com/structurizr/structurizr/issues/35).
- Adds support for [dynamic perspectives](https://docs.structurizr.com/dsl/cookbook/perspectives-dynamic/).
- Deprecates health checks in favour of dynamic perspectives.
- Fixes https://github.com/structurizr/structurizr/issues/36 (Workspace.trim() removes infrastructureNode when only element in deploymentNode).
- Fixes https://github.com/structurizr/structurizr/issues/37 (Workspace.trim() removes the origin of linked relationships).

### structurizr-client

- Removes deprecated Apache HttpClient usage (fixes https://github.com/structurizr/structurizr/issues/31).
- Adds `WorkspaceApiClient.putImage()` methods to upload an image to a workspace.

### structurizr-dsl

- Perspectives can be specified as a `perspective { ... }` block.

## v6.0.0 (1st February 2026)

### General

- Requires Java 21.

### structurizr-core

- Removes branding object.
- Removes Graphviz as an automatic layout implementation option.
- Fixes https://github.com/structurizr/structurizr/issues/5 (Deployment view animations don't include parent deployment nodes of software system instances).
- Adds a way to override relationship technologies in deployment environments.
- Filtered views can no longer be created on top of views with automatic layout enabled.

### structurizr-dsl

- Removes `branding` keyword.
- Adds a way to override relationship technologies in deployment environments.
- Adds support for "installed" themes (e.g. `theme amazon-web-services-2025.07`).
- Removes `theme default`.
