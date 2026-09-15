package com.structurizr.server.web.home;

import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.AbstractTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static com.structurizr.configuration.StructurizrProperties.WORKSPACES_PROPERTY;
import static org.junit.jupiter.api.Assertions.*;

public class LocalHomeControllerTests extends AbstractTestsBase {

    private LocalHomeController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new LocalHomeController();

        model = new ModelMap();
    }

    @Test
    void showHomePage() {
        Properties properties = new Properties();
        properties.setProperty(WORKSPACES_PROPERTY, "*");
        configureAsLocal(properties);

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1);
        WorkspaceMetadata workspace2 = new WorkspaceMetadata(2);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetadata> getWorkspaces() {
                return List.of(workspace1, workspace2);
            }
        });

        String result = controller.showHomePage("", 1, 1, model);
        assertEquals("home", result);

        assertTrue(((Collection<?>)model.getAttribute("workspaces")).contains(workspace1));
        assertFalse(((Collection<?>)model.getAttribute("workspaces")).contains(workspace2));

        assertTrue(((Collection<?>)model.getAttribute("quickNavigationItems")).contains(workspace1));
        assertTrue(((Collection<?>)model.getAttribute("quickNavigationItems")).contains(workspace2));
    }

}