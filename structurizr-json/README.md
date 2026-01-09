# Structurizr JSON

This directory contains the definition of the JSON schema for describing software architecture models based
upon the [C4 model](https://c4model.com),
and is the data storage format used by the [Structurizr tooling](https://docs.structurizr.com).

The JSON format is __not__ designed to be authored manually - the [Structurizr DSL](https://docs.structurizr.com/dsl)
is the recommended tool for creating a workspace. 

* [OpenAPI 3.0 definition ](structurizr.yaml)
* [Swagger Editor](https://editor.swagger.io/?url=https://raw.githubusercontent.com/structurizr/json/master/structurizr.yaml)

## Workspace validation rules

In addition to the JSON schema, workspaces are also subject to the following rules, which are implemented by the [Structurizr tooling](https://github.com/structurizr/structurizr):

- Element and relationship IDs must be unique.
- View `key` and `order` properties must be unique across all views.
- Software and people names must be unique.
- Container names must be unique within the context of a software system.
- Component names must be unique within the context of a container.
- Deployment node names must be unique with their parent context.
- Infrastructure node names must be unique with their parent context.
- All relationships from a source element to a destination element must have a unique description.
