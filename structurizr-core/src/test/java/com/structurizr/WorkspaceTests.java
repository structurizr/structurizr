package com.structurizr;

import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Format;
import com.structurizr.model.*;
import com.structurizr.view.RelationshipView;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.SystemLandscapeView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceTests {

    private Workspace workspace = new Workspace("Name", "Description");

    @Test
    void isEmpty_ReturnsTrue_WhenThereAreNoElementsViewsOrDocumentation() {
        workspace = new Workspace("Name", "Description");
        assertTrue(workspace.isEmpty());
    }

    @Test
    void isEmpty_ReturnsFalse_WhenThereAreElements() {
        workspace = new Workspace("Name", "Description");
        workspace.getModel().addPerson("Name", "Description");
        assertFalse(workspace.isEmpty());
    }

    @Test
    void isEmpty_ReturnsFalse_WhenThereAreViews() {
        workspace = new Workspace("Name", "Description");
        workspace.getViews().createSystemLandscapeView("key", "Description");
        assertFalse(workspace.isEmpty());
    }

    @Test
    void isEmpty_ReturnsFalse_WhenThereIsDocumentation() throws Exception {
        workspace = new Workspace("Name", "Description");
        Decision d = new Decision("1");
        d.setTitle("Title");
        d.setContent("Content");
        d.setStatus("Proposed");
        d.setFormat(Format.Markdown);
        workspace.getDocumentation().addDecision(d);
        assertFalse(workspace.isEmpty());
    }

    @Test
    void hydrate_DoesNotCrash() {
        Workspace workspace = new Workspace("Name", "Description");
        assertNotNull(workspace.getViews());
        assertNotNull(workspace.getDocumentation());

        // check that the hydrate method doesn't crash (it includes some method calls via reflection)
        workspace.hydrate();
    }

    @Test
    void remove_WhenACustomElementIsUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");

        CustomElement element = workspace.getModel().addCustomElement("Name");
        workspace.getViews().createCustomView("key", "Title", "Description").addDefaultElements();
        assertEquals(1, workspace.getModel().getCustomElements().size());

        workspace.remove(element);
        assertEquals(1, workspace.getModel().getCustomElements().size());
    }

    @Test
    void remove_WhenAPersonIsUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");

        Person person = workspace.getModel().addPerson("User");
        workspace.getViews().createSystemLandscapeView("key", "Description").addDefaultElements();
        assertEquals(1, workspace.getModel().getPeople().size());

        workspace.remove(person);
        assertEquals(1, workspace.getModel().getPeople().size());
    }

    @Test
    void remove_WhenASoftwareSystemIsUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        workspace.getViews().createSystemContextView(softwareSystem, "key", "Description").addDefaultElements();
        assertEquals(1, workspace.getModel().getElements().size());

        workspace.remove(softwareSystem);
        assertEquals(1, workspace.getModel().getElements().size());
    }

    @Test
    void remove_WhenAContainerIsUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        Container container = softwareSystem.addContainer("Container");
        workspace.getViews().createContainerView(softwareSystem, "key", "Description").addDefaultElements();
        assertEquals(2, workspace.getModel().getElements().size());

        workspace.remove(softwareSystem);
        assertEquals(2, workspace.getModel().getElements().size());

        workspace.remove(container);
        assertEquals(2, workspace.getModel().getElements().size());
    }

    @Test
    void remove_WhenAComponentIsUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        Container container = softwareSystem.addContainer("Container");
        Component component = container.addComponent("Component");
        workspace.getViews().createComponentView(container, "key", "Description").addDefaultElements();
        assertEquals(3, workspace.getModel().getElements().size());

        workspace.remove(softwareSystem);
        assertEquals(3, workspace.getModel().getElements().size());

        workspace.remove(container);
        assertEquals(3, workspace.getModel().getElements().size());

        workspace.remove(component);
        assertEquals(3, workspace.getModel().getElements().size());
    }

    @Test
    void remove_WhenASoftwareSystemInstanceIsUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        DeploymentNode deploymentNode = workspace.getModel().addDeploymentNode("Deployment Node");
        SoftwareSystemInstance softwareSystemInstance = deploymentNode.add(softwareSystem);
        workspace.getViews().createDeploymentView("key", "Description").addDefaultElements();
        assertEquals(3, workspace.getModel().getElements().size());

        workspace.remove(softwareSystem);
        assertEquals(3, workspace.getModel().getElements().size());

        workspace.remove(softwareSystemInstance);
        assertEquals(3, workspace.getModel().getElements().size());
    }

    @Test
    void remove_WhenAContainerInstanceIsUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        Container container = softwareSystem.addContainer("Container");
        DeploymentNode deploymentNode = workspace.getModel().addDeploymentNode("Deployment Node");
        ContainerInstance containerInstance = deploymentNode.add(container);
        workspace.getViews().createDeploymentView("key", "Description").addDefaultElements();
        assertEquals(4, workspace.getModel().getElements().size());

        workspace.remove(softwareSystem);
        assertEquals(4, workspace.getModel().getElements().size());

        workspace.remove(container);
        assertEquals(4, workspace.getModel().getElements().size());

        workspace.remove(containerInstance);
        assertEquals(4, workspace.getModel().getElements().size());
    }

    @Test
    void remove_WhenAnInfrastructureNodeIsUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");

        DeploymentNode deploymentNode = workspace.getModel().addDeploymentNode("Deployment Node");
        InfrastructureNode infrastructureNode = deploymentNode.addInfrastructureNode("Infrastructure Node");
        workspace.getViews().createDeploymentView("key", "Description").add(infrastructureNode);
        assertEquals(2, workspace.getModel().getElements().size());

        workspace.remove(infrastructureNode);
        assertEquals(2, workspace.getModel().getElements().size());

        workspace.remove(deploymentNode);
        assertEquals(2, workspace.getModel().getElements().size());
    }

    @Test
    void remove_WhenAnElementIsTheScopeOfAContainerView() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        workspace.getViews().createContainerView(softwareSystem, "key", "Description");
        assertEquals(1, workspace.getModel().getElements().size());

        workspace.remove(softwareSystem);
        assertEquals(1, workspace.getModel().getElements().size());
    }

    @Test
    void remove_WhenAnElementIsTheScopeOfAComponentView() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        Container container = softwareSystem.addContainer("Container");
        workspace.getViews().createComponentView(container, "key", "Description");
        assertEquals(2, workspace.getModel().getElements().size());

        workspace.remove(softwareSystem);
        assertEquals(2, workspace.getModel().getElements().size());
    }

    @Test
    void remove_WhenAnElementIsTheScopeOfADynamicView() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        workspace.getViews().createDynamicView(softwareSystem, "key", "Description");
        assertEquals(1, workspace.getModel().getElements().size());

        workspace.remove(softwareSystem);
        assertEquals(1, workspace.getModel().getElements().size());
    }

    @Test
    void remove_WhenAnElementIsTheScopeOfADeploymentView() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        workspace.getViews().createDeploymentView(softwareSystem, "key", "Description");
        assertEquals(1, workspace.getModel().getElements().size());

        workspace.remove(softwareSystem);
        assertEquals(1, workspace.getModel().getElements().size());
    }

    @Test
    void remove_WhenAnElementIsTheScopeOfAnImageView() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        workspace.getViews().createImageView(softwareSystem, "key");
        assertEquals(1, workspace.getModel().getElements().size());

        workspace.remove(softwareSystem);
        assertEquals(1, workspace.getModel().getElements().size());
    }

    @Test
    void trim_WhenAllElementsAreUnused() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getModel().setImpliedRelationshipsStrategy(new CreateImpliedRelationshipsUnlessAnyRelationshipExistsStrategy());

        CustomElement element = workspace.getModel().addCustomElement("Custom Element");
        Person user = workspace.getModel().addPerson("User");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        Container webapp = softwareSystem.addContainer("Web Application");
        Container database = softwareSystem.addContainer("Database");
        Component component = webapp.addComponent("Component");
        user.uses(component, "uses");
        component.uses(database, "uses");

        DeploymentNode live = workspace.getModel().addDeploymentNode("Live");
        DeploymentNode server1 = live.addDeploymentNode("Server 1");
        DeploymentNode server2 = live.addDeploymentNode("Server 2");
        ContainerInstance webappInstance = server1.add(webapp);
        ContainerInstance databaseInstance = server2.add(database);
        InfrastructureNode firewall = live.addInfrastructureNode("Firewall");
        webappInstance.uses(firewall, "", "");
        firewall.uses(databaseInstance, "", "");

        DeploymentNode dev = workspace.getModel().addDeploymentNode("Dev");
        SoftwareSystemInstance softwareSystemInstance = dev.add(softwareSystem);

        assertEquals(14, workspace.getModel().getElements().size());
        assertEquals(8, workspace.getModel().getRelationships().size());

        workspace.trim();
        assertEquals(0, workspace.getModel().getElements().size());
        assertEquals(0, workspace.getModel().getRelationships().size());
    }

    @Test
    void trim_WhenSomeElementsAreUnused() {
        Workspace workspace = new Workspace("Name", "Description");

        SoftwareSystem a = workspace.getModel().addSoftwareSystem("A");
        SoftwareSystem b = workspace.getModel().addSoftwareSystem("B");
        SoftwareSystem c = workspace.getModel().addSoftwareSystem("C");
        SoftwareSystem d = workspace.getModel().addSoftwareSystem("D");

        // a -> b -> c -> d
        Relationship ab = a.uses(b, "uses");
        Relationship bc = b.uses(c, "uses");
        Relationship cd = c.uses(d, "uses");

        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "Description");
        view.add(b);
        view.add(c);

        assertEquals(4, workspace.getModel().getElements().size());
        assertEquals(3, workspace.getModel().getRelationships().size());

        workspace.trim();
        assertEquals(2, workspace.getModel().getElements().size());
        assertEquals(1, workspace.getModel().getRelationships().size());
        assertTrue(workspace.getModel().contains(b));
        assertTrue(workspace.getModel().contains(c));
        assertTrue(workspace.getModel().contains(bc));
    }

    @Test
    void trim_WhenTheDestinationOfAnElementIsRemoved() {
        Workspace workspace = new Workspace("Name", "Description");
        SoftwareSystem a = workspace.getModel().addSoftwareSystem("A");
        SoftwareSystem b = workspace.getModel().addSoftwareSystem("B");
        a.uses(b, "Uses");

        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "Description");
        view.add(a);

        workspace.trim();

        assertEquals(0, a.getRelationships().size());
        assertTrue(workspace.getModel().contains(a));
        assertFalse(workspace.getModel().contains(b));
    }

    @Test
    void trim_WhenAnImpliedRelationshipIsUsedInAView() {
        Workspace workspace = new Workspace("Name");
        Person user = workspace.getModel().addPerson("User");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        Container container = softwareSystem.addContainer("Container");

        workspace.getModel().setImpliedRelationshipsStrategy(new CreateImpliedRelationshipsUnlessAnyRelationshipExistsStrategy());
        user.uses(container, "uses");
        assertTrue(user.hasEfferentRelationshipWith(softwareSystem));

        SystemContextView view = workspace.getViews().createSystemContextView(softwareSystem, "key");
        view.addDefaultElements();

        workspace.trim();

        assertTrue(user.hasEfferentRelationshipWith(softwareSystem));
        assertTrue(user.hasEfferentRelationshipWith(container));
        assertTrue(workspace.getModel().contains(user));
        assertTrue(workspace.getModel().contains(softwareSystem));
        assertTrue(workspace.getModel().contains(container));
    }

    @Test
    void removeRelationship_ThrowsAnException_WhenNoRelationshipIsSpecified() {
        Workspace workspace = new Workspace("Name", "Description");
        try {
            workspace.remove((Relationship)null);
            fail();
        } catch (Exception e) {
            assertEquals("A relationship must be specified.", e.getMessage());
        }
    }

    @Test
    void removeRelationship_DoesNotRemoveTheRelationshipWhenItIsUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");
        SoftwareSystem a = workspace.getModel().addSoftwareSystem("A");
        SoftwareSystem b = workspace.getModel().addSoftwareSystem("B");
        Relationship relationship = a.uses(b, "Uses");

        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "Description");
        view.addDefaultElements();

        boolean result = workspace.remove(relationship);

        assertFalse(result);
        assertEquals(1, a.getRelationships().size());
        assertTrue(a.hasEfferentRelationshipWith(b));
        assertTrue(view.isRelationshipInView(relationship));
    }

    @Test
    void removeRelationship_RemovesTheRelationshipWhenItIsNotUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");
        SoftwareSystem a = workspace.getModel().addSoftwareSystem("A");
        SoftwareSystem b = workspace.getModel().addSoftwareSystem("B");
        Relationship relationship = a.uses(b, "Uses");

        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key");
        view.add(a);
        view.add(b);
        view.remove(relationship);

        boolean result = workspace.remove(relationship);

        assertTrue(result);
        assertEquals(0, a.getRelationships().size());
        assertFalse(a.hasEfferentRelationshipWith(b));
    }

    @Test
    void removeRelationship_WhenALinkedRelationshipExistsAndIsUsedInAView() {
        Workspace workspace = new Workspace("Name", "Description");
        Person user = workspace.getModel().addPerson("User");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System");
        Container container = softwareSystem.addContainer("Container");

        workspace.getModel().setImpliedRelationshipsStrategy(new CreateImpliedRelationshipsUnlessAnyRelationshipExistsStrategy());
        Relationship relationship = user.uses(container, "Uses");

        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "Description");
        view.addDefaultElements();

        boolean result = workspace.remove(relationship);

        assertFalse(result);
        assertEquals(2, user.getRelationships().size());
        assertTrue(user.hasEfferentRelationshipWith(softwareSystem));
        assertTrue(user.hasEfferentRelationshipWith(container));

        RelationshipView rv = view.getRelationships().iterator().next();
        assertEquals(rv.getRelationship().getLinkedRelationshipId(), relationship.getId());
    }

}