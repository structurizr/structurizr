workspace {

    !adrs adrtools com.structurizr.example.ExampleDecisionImporter {
        exclude "README.md"
    }

    model {
        softwareSystem = softwareSystem "Software System" {
            !decisions adrtools {
                exclude "README.md"
            }

            container "Container" {
                !decisions madr madr {
                    exclude "README.*"
                }

                component "Component" {
                    !decisions log4brains log4brains {
                        exclude "README.md"
                    }
                }
            }
        }
    }

}