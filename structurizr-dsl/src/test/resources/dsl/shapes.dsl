workspace "Shapes" "An example of all shapes available in Structurizr." {

    model {
        softwareSystem "Box" "" "Box" 
        softwareSystem "Rounded Box" "" "RoundedBox"
        softwareSystem "Circle" "" "Circle"
        softwareSystem "Ellipse" "" "Ellipse"
        softwareSystem "Hexagon" "" "Hexagon"
        softwareSystem "Diamond" "" "Diamond"
        softwareSystem "Component" "" "Component"
        softwareSystem "Cylinder" "" "Cylinder"
        softwareSystem "Bucket" "" "Bucket"
        softwareSystem "Folder" "" "Folder"
        softwareSystem "Pipe" "" "Pipe"
        softwareSystem "Web Browser" "" "Web Browser"
        softwareSystem "Window" "" "Window"
        softwareSystem "Shell" "" "Shell"
        softwareSystem "Terminal" "" "Terminal"
        softwareSystem "Mobile Device Portrait" "" "Mobile Device Portrait"
        softwareSystem "Mobile Device Landscape" "" "Mobile Device Landscape"
	    person "Person"
        softwareSystem "Robot" "" "Robot"
    }

    views {
        systemLandscape "shapes" "An example of all shapes available in Structurizr." {
            include *
        }

        styles {
            element "Element" {
                color #000000
                stroke #000000
                strokeWidth 7
                fontsize 35
                description false
                metadata false
            }

            element "Box" {
                shape "Box"
            }
            element "RoundedBox" {
                shape "RoundedBox"
            }
            element "Circle" {
                shape "Circle"
            }
            element "Ellipse" {
                shape "Ellipse"
            }
            element "Hexagon" {
                shape "Hexagon"
            }
            element "Diamond" {
                shape "Diamond"
            }
            element "Component" {
                shape "Component"
            }
            element "Cylinder" {
                shape "Cylinder"
            }
            element "Bucket" {
                shape bucket
            }
            element "Folder" {
                shape "Folder"
            }
            element "Pipe" {
                shape "Pipe"
            }

            element "Web Browser" {
                shape "WebBrowser"
            }
            element "Window" {
                shape "Window"
            }
            element "Shell" {
                shape shell
            }
            element "Terminal" {
                shape terminal
            }

            element "Mobile Device Portrait" {
                shape "MobileDevicePortrait"
            }
            element "Mobile Device Landscape" {
                shape "MobileDeviceLandscape"
            }

            element "Person" {
                shape "Person"
            }
            element "Robot" {
                shape "Robot"
            }
        }

    }

}