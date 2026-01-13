workspace {

    !script groovy {
        workspace.model.addPerson("Groovy");
    }

    !script kotlin {
        workspace.model.addPerson("Kotlin");
    }

    !script ruby {
        workspace.model.addPerson("Ruby");
    }

    model {
        user = person "User" {
            !script groovy {
                element.addTags("Groovy")
            }
        }

        softwareSystem "Software System" {
            user -> this {
                !script groovy {
                    relationship.addTags("Groovy")
                }
            }
        }
    }

    views {
        systemLandscape {
            !script groovy {
                view.description = "Groovy"
            }
        }
    }

}