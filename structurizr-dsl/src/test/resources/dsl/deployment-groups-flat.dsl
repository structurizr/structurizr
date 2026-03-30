workspace {

    model {
        softwareSystem = softwareSystem "Software System" {
            database = container "DB"
            api = container "API" {
                -> database "Uses"
            }
        }

        deploymentEnvironment "WithoutDeploymentGroups" {
            deploymentNode "Server 1" {
                containerInstance api
                containerInstance database
            }
            deploymentNode "Server 2" {
                containerInstance api
                containerInstance database
            }
        }

        deploymentEnvironment "WithSpecifiedDeploymentGroups" {
            serviceInstance1 = deploymentGroup "Service Instance 1"
            serviceInstance2 = deploymentGroup "Service Instance 2"
            deploymentNode "Server 1" {
                containerInstance api serviceInstance1
                containerInstance database serviceInstance1
            }
            deploymentNode "Server 2" {
                containerInstance api serviceInstance2
                containerInstance database serviceInstance2
            }
        }

        deploymentEnvironment "WithInheritedDeploymentGroups" {
            deploymentNode "Server 1" {
                deploymentGroup serviceInstance1
                containerInstance api
                containerInstance database
            }
            deploymentNode "Server 2" {
                deploymentGroup serviceInstance2
                containerInstance api
                containerInstance database
            }
        }
    }

}