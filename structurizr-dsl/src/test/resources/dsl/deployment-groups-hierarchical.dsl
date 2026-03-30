workspace {

    !identifiers hierarchical

    model {
        softwareSystem = softwareSystem "Software System" {
            database = container "DB"
            api = container "API" {
                -> database "Uses"
            }
        }

        deploymentEnvironment "WithoutDeploymentGroups" {
            deploymentNode "Server 1" {
                containerInstance softwareSystem.api
                containerInstance softwareSystem.database
            }
            deploymentNode "Server 2" {
                containerInstance softwareSystem.api
                containerInstance softwareSystem.database
            }
        }

        deploymentEnvironment "WithSpecifiedDeploymentGroups" {
            serviceInstance1 = deploymentGroup "Service Instance 1"
            serviceInstance2 = deploymentGroup "Service Instance 2"
            deploymentNode "Server 1" {
                containerInstance softwareSystem.api serviceInstance1
                containerInstance softwareSystem.database serviceInstance1
            }
            deploymentNode "Server 2" {
                containerInstance softwareSystem.api serviceInstance2
                containerInstance softwareSystem.database serviceInstance2
            }
        }

        deploymentEnvironment "WithSpecifiedDeploymentGroupsAgain" {
            serviceInstance1 = deploymentGroup "Service Instance 1"
            serviceInstance2 = deploymentGroup "Service Instance 2"
            deploymentNode "Server 1" {
                containerInstance softwareSystem.api serviceInstance1
                containerInstance softwareSystem.database serviceInstance1
            }
            deploymentNode "Server 2" {
                containerInstance softwareSystem.api serviceInstance2
                containerInstance softwareSystem.database serviceInstance2
            }
        }

        deploymentEnvironment "WithInheritedDeploymentGroups" {
            serviceInstance1 = deploymentGroup "Service Instance 1"
            serviceInstance2 = deploymentGroup "Service Instance 2"
            deploymentNode "Server 1" {
                deploymentGroup serviceInstance1
                containerInstance softwareSystem.api
                containerInstance softwareSystem.database
            }
            deploymentNode "Server 2" {
                deploymentGroup serviceInstance2
                containerInstance softwareSystem.api
                containerInstance softwareSystem.database
            }
        }
    }

}