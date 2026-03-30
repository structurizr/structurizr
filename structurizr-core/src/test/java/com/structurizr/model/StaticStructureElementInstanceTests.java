package com.structurizr.model;

import com.structurizr.Workspace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StaticStructureElementInstanceTests {

    @Test
    void inSameDeploymentGroup_ReturnsTrue_WhenBothInstancesAreInTheSameSpecifiedGroup() {
        ContainerInstance instance1 = new ContainerInstance(null, 1, "live", new String[]{});
        ContainerInstance instance2 = new ContainerInstance(null, 1, "live", new String[]{});

        assertTrue(instance1.inSameDeploymentGroup(instance2));
        assertTrue(instance2.inSameDeploymentGroup(instance1));

        instance1 = new ContainerInstance(null, 1, "live", new String[]{ "Instance 1" });
        instance2 = new ContainerInstance(null, 1, "live", new String[]{ "Instance 1" });

        assertTrue(instance1.inSameDeploymentGroup(instance2));
        assertTrue(instance2.inSameDeploymentGroup(instance1));
    }

    @Test
    void inSameDeploymentGroup_ReturnsFalse_WhenBothInstancesAreNotInTheSameSpecifiedGroup() {
        ContainerInstance instance1 = new ContainerInstance(null, 1, "live", new String[]{ "Instance 1" });
        ContainerInstance instance2 = new ContainerInstance(null, 1, "live", new String[]{ "Instance 2" });

        assertFalse(instance1.inSameDeploymentGroup(instance2));
        assertFalse(instance2.inSameDeploymentGroup(instance1));
    }

    @Test
    void inSameDeploymentGroup_ReturnsTrue_WhenBothInstancesAreInTheSameInheritedGroup() {
        Workspace workspace = new Workspace("Name");
        Model model = workspace.getModel();

        Container container = model.addSoftwareSystem("Software System").addContainer("Container");
        DeploymentNode deploymentNode1 = model.addDeploymentNode("Deployment Node 1");
        DeploymentNode deploymentNode2 = deploymentNode1.addDeploymentNode("Deployment Node 2");
        deploymentNode1.addDeploymentGroup("Instance 1");
        ContainerInstance instance1 = deploymentNode2.add(container);
        ContainerInstance instance2 = deploymentNode2.add(container);

        assertTrue(instance1.inSameDeploymentGroup(instance2));
        assertTrue(instance2.inSameDeploymentGroup(instance1));
    }

    @Test
    void inSameDeploymentGroup_ReturnsFalse_WhenBothInstancesAreNotInTheSameInheritedGroup() {
        Workspace workspace = new Workspace("Name");
        Model model = workspace.getModel();

        Container container = model.addSoftwareSystem("Software System").addContainer("Container");
        DeploymentNode deploymentNode1 = model.addDeploymentNode("Deployment Node 1");
        deploymentNode1.addDeploymentGroup("Instance 1");

        DeploymentNode deploymentNode2 = model.addDeploymentNode("Deployment Node 2");
        deploymentNode2.addDeploymentGroup("Instance 2");

        ContainerInstance instance1 = deploymentNode1.add(container);
        ContainerInstance instance2 = deploymentNode2.add(container);

        assertFalse(instance1.inSameDeploymentGroup(instance2));
        assertFalse(instance2.inSameDeploymentGroup(instance1));
    }

}