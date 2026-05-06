workspace extends https://raw.githubusercontent.com/structurizr/structurizr/refs/heads/main/structurizr-dsl/src/test/resources/dsl/extend/workspace.dsl {

    model {
        !element softwareSystem1 {
            webapp = container "Web Application"
        }

        user -> softwareSystem1 "Uses"
        softwareSystem3.webapp -> softwareSystem3.db
    }

}