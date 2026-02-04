# Changelog

## v6.0.1 (unreleased)

## structurizr-core

- Normalises line endings when importing Markdown/AsciiDoc content (documentation and decisions).

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
