package com.structurizr.server.util;

import com.structurizr.util.DocumentationScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentationScopeTests {

    @Test
    void toScope_ForWorkspace() {
        assertEquals("*", DocumentationScope.format(null, null, null));
    }

    @Test
    void toScope_ForSoftwareSystem() {
        assertEquals("A", DocumentationScope.format("A", null, null));
    }

    @Test
    void toScope_ForContainer() {
        assertEquals("A/B", DocumentationScope.format("A", "B", null));
    }

    @Test
    void toScope_ForComponent() {
        assertEquals("A/B/C", DocumentationScope.format("A", "B", "C"));
    }

}