# Changelog

## v6.1.0 (unreleased)

## structurizr-core

- Normalises line endings when importing Markdown/AsciiDoc content (documentation and decisions).
- Adds support for dynamic perspectives.
- Deprecates health checks in favour of dynamic perspectives.
- Fixes https://github.com/structurizr/structurizr/issues/35 (Group labels truncated when group name does not contain the group separator).
- Fixes https://github.com/structurizr/structurizr/issues/36 (Workspace.trim() removes infrastructureNode when only element in deploymentNode).
- Fixes https://github.com/structurizr/structurizr/issues/37 (Workspace.trim() removes the origin of linked relationships).

## structurizr-dsl

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
