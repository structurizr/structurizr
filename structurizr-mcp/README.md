# Structurizr MCP server

An experimental MCP server that provides the following tools:

- Get a JSON workspace from a file
  - Parameters:
    - Filename (required)
  - Returns a Structurizr workspace
- Parse a DSL file
    - Parameters:
      - Filename (required)
  - Returns a Structurizr workspace
- Get a single workspace from a Structurizr server
  - Parameters:
    - Structurizr server URL (required)
    - Workspace ID (required)
    - API key (not required if running the server with authentication disabled)
  - Returns a Structurizr workspace
- Get all workspaces from a Structurizr server
  - Parameters:
    - Structurizr server URL (required)
    - Admin API key (not required if running the server with authentication disabled)
  - Returns a collection of Structurizr workspaces

## Building from source

Prerequisites:

- Java 21
- Maven 3.9.x

To build:

```
git clone https://github.com/structurizr/structurizr.git
cd structurizr
mvn -Dmaven.test.skip=true package -pl structurizr-mcp -am
```

If successful, there will be a `structurizr-mcp-1.0.0.war` file in `structurizr-mcp/target`. 

## Claude Desktop configuration

```
{
  ...
  "mcpServers": {
    "structurizr-mcp": {
    "command": "java",
    "args": [
    "-Dlogging.pattern.console=",
    "-jar",
    "/path/structurizr-mcp-1.0.0.war"]
    }
  }
  ...
}
```