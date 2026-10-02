package com.structurizr.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class InspectCommandTests {

    @Test
    public void run() throws Exception {
        String[] args = {
                "inspect",
                "-workspace", "src/test/dsl/workspace.dsl"
        };
        boolean success = new InspectCommand().run(args);
        assertFalse(success);
    }

}