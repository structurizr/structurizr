workspace {

    !docs docs/workspace com.structurizr.example.ExampleDocumentationImporter

    model {
        user = person "User"
        softwareSystem = softwareSystem "Software System" {
            !docs docs/softwaresystem {
                exclude "README.md"
            }

            container "Container" {
                !docs docs/softwaresystem/container {
                    exclude "README.*"
                }

                component "Component" {
                    !docs docs/softwaresystem/container/component/1.md
                }
            }
        }

        user -> softwareSystem "Uses"
    }

    views {
        systemContext softwareSystem "Diagram1" {
            include *
            autoLayout
        }

        theme default
    }

}