package com.structurizr.util;

import java.util.Random;

public class DslTemplate {

    private static final String DSL_TEMPLATE = """
workspace "%s" "%s" {
    
    !identifiers hierarchical
    
    model {
        u = person "User"
        ss = softwareSystem "Software System" {
            wa = container "Web Application"
            db = container "Database Schema" {
                tags "Database"
            }
        }
    
        u -> ss.wa "Uses"
        ss.wa -> ss.db "Reads from and writes to"
    }
    
    views {
        systemContext ss "Diagram1" {
            include *
        }
    
        container ss "Diagram2" {
            include *
        }
    
        styles {
            element "Element" {
                color %s
                stroke %s
                strokeWidth 7
                shape roundedbox
            }
            element "Person" {
                shape person
            }
            element "Database" {
                shape cylinder
            }
            element "Boundary" {
                strokeWidth 5
            }
            relationship "Relationship" {
                thickness 4
            }
        }
    }
    
    configuration {
        scope softwaresystem
    }
    
}""";

    private final static String[][] COLOURS = {
            { "#0773af" }, // blue
            { "#55aa55" }, // green
            { "#d9232b" }, // red
            { "#f88728" }, // orange
            { "#f8289c" }, // pink
            { "#9a28f8" }, // purple
    };

    public static String generate(String name, String description) {
        int randomInt = new Random().nextInt(COLOURS.length);
        String[] colours = COLOURS[randomInt];
        return String.format(DSL_TEMPLATE, name, description, colours[0], colours[0]);
    }

}