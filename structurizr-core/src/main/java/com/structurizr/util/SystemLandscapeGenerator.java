package com.structurizr.util;

import com.structurizr.Workspace;
import com.structurizr.configuration.WorkspaceScope;
import com.structurizr.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * 1. Find all people and software systems across all workspaces.
 */
public class SystemLandscapeGenerator {

    private static final Log log = LogFactory.getLog(SystemLandscapeGenerator.class);

    private RelationshipsStrategy relationshipStrategy = RelationshipsStrategy.First;

    public void setRelationshipStrategy(RelationshipsStrategy relationshipStrategy) {
        this.relationshipStrategy = relationshipStrategy;
    }

    public Workspace generate(Collection<Workspace> workspaces) {
        if (workspaces == null || workspaces.isEmpty()) {
            throw new IllegalArgumentException("Two or more workspaces must be provided");
        }

        // create a system landscape workspace based upon the system catalog
        // (this defines software systems, but not relationships)
        Workspace systemLandscapeWorkspace = new Workspace("System Landscape");
        systemLandscapeWorkspace.getConfiguration().setScope(WorkspaceScope.Landscape);

        // find all people and software systems
        for (Workspace workspace : workspaces) {
            for (Person person : workspace.getModel().getPeople()) {
                clone(person, systemLandscapeWorkspace);
            }
            for (SoftwareSystem softwareSystem : workspace.getModel().getSoftwareSystems()) {
                clone(softwareSystem, systemLandscapeWorkspace);
            }
        }

        for (Workspace workspace : workspaces) {
            findAndCloneRelationships(workspace, systemLandscapeWorkspace);
            hyperlinkSoftwareSystem(workspace, systemLandscapeWorkspace);
        }

        return systemLandscapeWorkspace;
    }

    private void findAndCloneRelationships(Workspace source, Workspace destination) {
        for (Relationship relationship : source.getModel().getRelationships()) {
            if (isPersonOrSoftwareSystem(relationship.getSource()) && isPersonOrSoftwareSystem(relationship.getDestination())) {
                cloneRelationship(relationship, destination.getModel());
            }
        }
    }

    private boolean isPersonOrSoftwareSystem(Element element) {
        return element instanceof SoftwareSystem || element instanceof Person;
    }

    private void cloneRelationship(Relationship relationship, Model model) {
        Element source = findElement(relationship.getSource(), model);
        Element destination = findElement(relationship.getDestination(), model);

        if (source != null && destination != null) {
            if (relationshipStrategy == RelationshipsStrategy.First) {
                if (!source.hasEfferentRelationshipWith(destination)) {
                    cloneRelationship(relationship, source, destination);
                }
            } else if (relationshipStrategy == RelationshipsStrategy.All) {
                if (!source.hasEfferentRelationshipWith(destination, relationship.getDescription())) {
                    cloneRelationship(relationship, source, destination);
                }
            }
        }
    }

    private void cloneRelationship(Relationship relationship, Element source, Element destination) {
        Relationship clonedRelationship = null;

        if (source instanceof Person) {
            if (destination instanceof Person) {
                clonedRelationship = ((Person)source).interactsWith((Person)destination, relationship.getDescription(), relationship.getTechnology());
            } else if (destination instanceof SoftwareSystem) {
                clonedRelationship = ((Person)source).uses((SoftwareSystem)destination, relationship.getDescription(), relationship.getTechnology());
            }
        } else if (source instanceof SoftwareSystem) {
            if (destination instanceof Person) {
                clonedRelationship = ((SoftwareSystem)source).delivers((Person)destination, relationship.getDescription(), relationship.getTechnology());
            } else if (destination instanceof SoftwareSystem) {
                clonedRelationship = ((SoftwareSystem)source).uses((SoftwareSystem)destination, relationship.getDescription(), relationship.getTechnology());
            }
        }

        if (clonedRelationship != null) {
            clone(relationship, clonedRelationship);
        }
    }

    private Element findElement(Element element, Model model) {
        Optional<Person> person = model.getPeople().stream().filter(p -> p.getName().equals(element.getName())).findFirst();
        if (person.isPresent()) {
            return person.get();
        }

        Optional<SoftwareSystem> softwareSystem = model.getSoftwareSystems().stream().filter(ss -> ss.getName().equals(element.getName())).findFirst();
        if (softwareSystem.isPresent()) {
            return softwareSystem.get();
        }

        log.warn("Person or software system with name \"" + element.getName() + "\" not found.");
        return null;
    }

    private SoftwareSystem findScopedSoftwareSystem(Workspace workspace) {
        return workspace.getModel().getSoftwareSystems().stream().filter(ss -> !ss.getContainers().isEmpty()).findFirst().orElse(null);
    }

    private void hyperlinkSoftwareSystem(Workspace workspace, Workspace systemLandscapeWorkspace) {
        // add a hyperlink from the software system in the system landscape workspace
        // to the workspace describing the software system
        SoftwareSystem softwareSystem = findScopedSoftwareSystem(workspace);
        if (softwareSystem != null) {
            Element ss = findElement(softwareSystem, systemLandscapeWorkspace.getModel());
            if (ss != null) {
                ss.setUrl("{workspace:" + workspace.getId() + "}/diagrams");
            }
        }
    }

    private void clone(Person source, Workspace workspace) {
        Person person = workspace.getModel().getPersonWithName(source.getName());
        if (person == null) {
            person = workspace.getModel().addPerson(source.getName());
        }

        person.setDescription(source.getDescription());
        clone(source, person);
    }

    private void clone(SoftwareSystem source, Workspace workspace) {
        SoftwareSystem softwareSystem = workspace.getModel().getSoftwareSystemWithName(source.getName());
        if (softwareSystem == null) {
            softwareSystem = workspace.getModel().addSoftwareSystem(source.getName());
        }

        softwareSystem.setDescription(source.getDescription());
        clone(source, softwareSystem);
    }

    private void clone(ModelItem source, ModelItem destination) {
        destination.setUrl(source.getUrl());
        destination.addTags(destination.getTags());

        for (String name : source.getProperties().keySet()) {
            destination.addProperty(name, source.getProperties().get(name));
        }

        for (Perspective perspective : source.getPerspectives()) {
            if (!destination.hasPerspective(perspective.getName())) {
                destination.addPerspective(perspective);
            }
        }
    }

    public enum RelationshipsStrategy {
        First,
        All
    }

}