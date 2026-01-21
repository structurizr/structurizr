# Changelog for application

## 2026.xx.xx (unreleased)

- local+server: Upgrades JointJS from 3.x to 4.x
- local+server: Removes Graphviz integration from the diagram viewer/editor; uses browser-based Dagre instead.
- local+server: Removes font configuration.
- local+server: Workspace logo now sourced from an element style named `Workspace:Logo`.
- local: Diagrams are now editable after clicking the "edit" button on the diagram viewer.
- local: Removes support for Lite's `STRUCTURIZR_WORKSPACE_PATH` and `STRUCTURIZR_WORKSPACE_FILENAME` (use multi-workspace mode instead).
- local: Removes support for Lite's auto-sync.
- server: Authentication is disabled by default.
- server: Adds API endpoints to get branches and delete a branch (https://github.com/structurizr/structurizr/issues/8).
- branches: Adds a branches command to list workspace branches (https://github.com/structurizr/structurizr/issues/7).
- delete: Adds a delete command to delete a workspace branch (https://github.com/structurizr/structurizr/issues/7).
- playground: Adds a new [playground server](https://playground.structurizr.com).