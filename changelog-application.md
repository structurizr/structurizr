# Changelog

## unreleased

- Fixes https://github.com/structurizr/structurizr/issues/22 (Diagram-as-text plugin rendering (Mermaid) for Markdown docs not compatible for Windows line endings).
- Adds perspective specific styles for elements and relationships.
- Adds support for dynamic perspectives.
- Adds a way to install themes when using `push`, `validate`, etc commands - see [Installing themes](https://docs.structurizr.com/server/diagrams/themes#installing-themes).
- Removes `-themes` from the `export` command in favour of the above.

### server

- Changes the output from the `GET /api/workspace/<id>/branches` endpoint.
- Fixes https://github.com/structurizr/structurizr/issues/34 (JSON endpoint produces text/plain instead of application/json).
- Adds a workspace size limit of 1MB (configurable via `structurizr.workspace.maxsize`).
- Renames configuration property `structurizr.maxworkspaceversions` to `structurizr.workspace.maxversions`.

### local

- Adds a workspace size limit of 1MB (configurable via `structurizr.workspace.maxsize`).

### playground

- Adds a workspace size limit of 1MB (configurable via `structurizr.workspace.maxsize`).

### branches

- Adds a `-json` flag to enable JSON output.

### create

- Adds a `create` command to create a workspace on a Structurizr server.

### delete

- Adds the ability to delete a workspace (admin API key is required).
- Confirmation prompt added.
- Adds a `-force` flag to force deletion (non-interactive/quiet mode).

### pull

- Removes support for `-id *` in favour of making `-id` optional.
- Adds a `-json` flag to enable JSON output (works only when pulling a single workspace).

### inspect

- Uses stdout rather than `log.info()`.

### list

- Uses stdout rather than `log.info()`.

## 2026.02.01

- Adds `com.structurizr.dsl.plugin.documentation.PlantUML` plugin.
- Adds `com.structurizr.dsl.plugin.documentation.Mermaid` plugin.

### local

- Removes Graphviz integration from the diagram viewer/editor; uses browser-based Dagre instead.
- Removes font and icon configuration via "branding".
- A workspace icon can now be sourced from an element style named `Workspace:Icon`.
- Diagrams are now editable after clicking the "edit" button on the diagram viewer.
- Removes support for Lite's `STRUCTURIZR_WORKSPACE_PATH` and `STRUCTURIZR_WORKSPACE_FILENAME` (use multi-workspace mode instead).
- Removes support for Lite's auto-sync.
- Diagram editor UI/UX improvements and bug fixes.
- Adds a theme browser to display installed themes (/themes).
- Configuration items can be specified as `structurizr.xxx` properties in `structurizr.properties` file, or via `STRUCTURIZR_XXX` environment variables.
- Adds a way to install themes via a property named `structurizr.themes`.

### server

- Open core
  - Removes Graphviz integration from the diagram viewer/editor; uses browser-based Dagre instead.
  - Removes font and icon configuration via "branding".
  - A workspace icon can now be sourced from an element style named `Workspace:Icon`.
  - Authentication is disabled by default.
  - No API key required for API calls when authentication is disabled.
  - Adds API endpoint to get branches.
  - Adds API endpoint to delete a branch.
  - Simplifies the iframe embed code.
  - Added ability to publish SVG exports for image embeds.
  - Diagram editor UI/UX improvements and bug fixes.
  - Adds a theme browser to display installed themes (/themes).
  - Branch can be specified with iframe embed.
  - Tags can be specified with iframe embed.
  - Removes the `/theme` endpoints for workspaces.
  - Adds a way to install themes via a property named `structurizr.themes`.
- Closed extensions
  - Adds a `fixed` authentication variant (username/password specified via config).
  - Simplifies role-based access.
  - Simplifies workspace visibility (public and sharing links).
  - Simplifies API authorisation (HMAC scheme replaced with API key).
  - Adds a way to regenerate workspace API keys from the UI (workspace settings page).

### playground

- Adds a new [playground](https://playground.structurizr.com).

### pull

- Workspace ID of `*` can be used to pull all workspaces (requires the admin API key).

### branches

- Adds a `branches` command to list workspace branches.

### delete

- Adds a `delete` command to delete a workspace branch.

### export

- Adds a way to suppress the introduction modal on the static site export.
- `Workspace:Icon` element style icon used on static site introduction modal.