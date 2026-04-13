package com.structurizr.export.plantuml;

import com.structurizr.export.AbstractDiagramExporter;
import com.structurizr.export.Diagram;
import com.structurizr.export.IndentingWriter;
import com.structurizr.model.*;
import com.structurizr.util.StringUtils;
import com.structurizr.view.*;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;

import static java.lang.String.format;

public abstract class AbstractPlantUMLExporter extends AbstractDiagramExporter {

    protected static final int DEFAULT_FONT_SIZE = 24;

    public static final String PLANTUML_TITLE_PROPERTY = "plantuml.title";
    public static final String PLANTUML_INCLUDES_PROPERTY = "plantuml.includes";
    public static final String PLANTUML_SKINPARAMS_PROPERTY = "plantuml.skinparams";
    public static final String PLANTUML_ANIMATION_PROPERTY = "plantuml.animation";
    public static final String PLANTUML_SEQUENCE_DIAGRAM_PROPERTY = "plantuml.sequenceDiagram";
    public static final String PLANTUML_BOUNDARIES = "plantuml.boundaries";

    public static final String DIAGRAM_TITLE_TAG = "Diagram:Title";
    public static final String DIAGRAM_DESCRIPTION_TAG = "Diagram:Description";

    private final Map<String, String> skinParams = new LinkedHashMap<>();

    protected Map<String, String> getSkinParams() {
        return skinParams;
    }

    public void addSkinParam(String name, String value) {
        skinParams.put(name, value);
    }

    public void clearSkinParams() {
        skinParams.clear();
    }

    public AbstractPlantUMLExporter() {
        this(ColorScheme.Light);
    }

    public AbstractPlantUMLExporter(ColorScheme colorScheme) {
        super(colorScheme);
    }

    String plantUMLShapeOf(ModelView view, Element element) {
        Shape shape = findElementStyle(view, element).getShape();

        return plantUMLShapeOf(shape);
    }

    String plantUMLShapeOf(Shape shape) {
        switch(shape) {
            case Person:
            case Robot:
                return "person";
            case Component:
                return "component";
            case Cylinder:
                return "database";
            case Folder:
                return "folder";
            case Ellipse:
            case Circle:
                return "storage";
            case Hexagon:
                return "hexagon";
            case Pipe:
                return "queue";
            default:
                return "rectangle";
        }
    }

    String plantumlSequenceType(ModelView view, Element element) {
        Shape shape = findElementStyle(view, element).getShape();

        switch(shape) {
            case Box:
                return "participant";
            case Person:
                return "actor";
            case Cylinder:
                return "database";
            case Folder:
                return "collections";
            case Ellipse:
            case Circle:
                return "entity";
            default:
                return "participant";
        }
    }

    String idOf(ModelItem modelItem) {
        if (modelItem instanceof Element) {
            Element element = (Element)modelItem;
            if (element.getParent() == null) {
                if (element instanceof DeploymentNode) {
                    DeploymentNode dn = (DeploymentNode)element;
                    return filter(dn.getEnvironment()) + "." + id(dn);
                } else {
                    return id(element);
                }
            } else {
                return idOf(element.getParent()) + "." + id(modelItem);
            }
        }

        return id(modelItem);
    }

    private String id(ModelItem modelItem) {
        if (modelItem instanceof Person) {
            return id((Person)modelItem);
        } else  if (modelItem instanceof SoftwareSystem) {
            return id((SoftwareSystem)modelItem);
        } else  if (modelItem instanceof Container) {
            return id((Container)modelItem);
        } else  if (modelItem instanceof Component) {
            return id((Component)modelItem);
        } else  if (modelItem instanceof DeploymentNode) {
            return id((DeploymentNode)modelItem);
        } else  if (modelItem instanceof InfrastructureNode) {
            return id((InfrastructureNode)modelItem);
        } else  if (modelItem instanceof SoftwareSystemInstance) {
            return id((SoftwareSystemInstance)modelItem);
        } else  if (modelItem instanceof ContainerInstance) {
            return id((ContainerInstance)modelItem);
        }

        return modelItem.getId();
    }

    private String id(Person person) {
        return filter(person.getName());
    }

    private String id(SoftwareSystem softwareSystem) {
        return filter(softwareSystem.getName());
    }

    private String id(Container container) {
        return filter(container.getName());
    }

    private String id(Component component) {
        return filter(component.getName());
    }

    private String id(DeploymentNode deploymentNode) {
        return filter(deploymentNode.getName());
    }

    private String id(InfrastructureNode infrastructureNode) {
        return filter(infrastructureNode.getName());
    }

    private String id(SoftwareSystemInstance softwareSystemInstance) {
        return filter(softwareSystemInstance.getName()) + "_" + softwareSystemInstance.getInstanceId();
    }

    private String id(ContainerInstance containerInstance) {
        return filter(containerInstance.getName()) + "_" + containerInstance.getInstanceId();
    }

    private String filter(String s) {
        return s.replaceAll("(?U)\\W", "");
    }

    protected boolean includeTitle(ModelView view) {
        return "true".equals(getViewOrViewSetProperty(view, PLANTUML_TITLE_PROPERTY, "true"));
    }

    @Override
    protected boolean isAnimationSupported(ModelView view) {
        return "true".equalsIgnoreCase(getViewOrViewSetProperty(view, PLANTUML_ANIMATION_PROPERTY, "false"));
    }

    @Override
    protected void writeHeader(ModelView view, IndentingWriter writer) {
        writer.writeLine("@startuml");

        if (includeTitle(view)) {
            ElementStyle titleStyle = findElementStyle(view, DIAGRAM_TITLE_TAG);
            ElementStyle descriptionStyle = findElementStyle(view, DIAGRAM_DESCRIPTION_TAG);

            String title = view.getTitle();
            if (StringUtils.isNullOrEmpty(title)) {
                title = view.getName();
            }

            String description = view.getDescription();
            if (StringUtils.isNullOrEmpty(description)) {
                writer.writeLine(
                        String.format(
                                "title <size:%s>%s</size>",
                                titleStyle != null ? titleStyle.getFontSize() : DEFAULT_FONT_SIZE,
                                title
                        )
                );
            } else {
                writer.writeLine(
                        String.format(
                                "title <size:%s>%s</size>\\n<size:%s>%s</size>",
                                titleStyle != null ? titleStyle.getFontSize() : DEFAULT_FONT_SIZE,
                                title,
                                descriptionStyle != null ? descriptionStyle.getFontSize() : DEFAULT_FONT_SIZE,
                                description
                        )
                );
            }
        }

        writer.writeLine();
        writer.writeLine("set separator none");
    }

    protected void writeSkinParams(ModelView view, IndentingWriter writer) {
        String commaSeparatedSkinParams = getViewOrViewSetProperty(view, PLANTUML_SKINPARAMS_PROPERTY, "");

        if (!skinParams.isEmpty() || !StringUtils.isNullOrEmpty(commaSeparatedSkinParams)) {
            writer.writeLine();
            writer.writeLine("skinparam {");
            writer.indent();

            for (final String name : skinParams.keySet()) {
                writer.writeLine(format("%s %s", name, skinParams.get(name)));
            }

            String[] skinparams = commaSeparatedSkinParams.split(",");
            for (String skinparam : skinparams) {
                if (!StringUtils.isNullOrEmpty(skinparam) && skinparam.contains("=")) {
                    skinparam = skinparam.trim();
                    String[] parts = skinparam.split("=");
                    writer.writeLine(format("%s %s", parts[0], parts[1]));
                }
            }

            writer.outdent();
            writer.writeLine("}");
        }

        writer.writeLine();
    }

    protected void writeIncludes(ModelView view, IndentingWriter writer) {
        String commaSeparatedIncludes = getViewOrViewSetProperty(view, PLANTUML_INCLUDES_PROPERTY, "");
        if (!StringUtils.isNullOrEmpty(commaSeparatedIncludes)) {
            String[] includes = commaSeparatedIncludes.split(",");

            for (String include : includes) {
                if (!StringUtils.isNullOrEmpty(include)) {
                    include = include.trim();
                    writer.writeLine("!include " + include);
                }
            }
            writer.writeLine();
        }
    }

    protected boolean renderAsSequenceDiagram(ModelView view) {
        return view instanceof DynamicView && "true".equalsIgnoreCase(getViewOrViewSetProperty(view, PLANTUML_SEQUENCE_DIAGRAM_PROPERTY, "false"));
    }

    @Override
    public Diagram export(DynamicView view) {
        if (renderAsSequenceDiagram(view)) {
            IndentingWriter writer = new IndentingWriter();
            writeHeader(view, writer);

            Set<Element> elements = new LinkedHashSet<>();
            for (RelationshipView relationshipView : view.getRelationships()) {
                elements.add(relationshipView.getRelationship().getSource());
                elements.add(relationshipView.getRelationship().getDestination());
            }

            if ("true".equalsIgnoreCase(getViewOrViewSetProperty(view, PLANTUML_BOUNDARIES, "true"))) {
                List<Element> elementsInWriteOrder = new ArrayList<>();
                for (Element element : elements) {
                    if (element.getParent() == null) {
                        elementsInWriteOrder.add(element);
                    } else {
                        List<Element> parents = new ArrayList<>();
                        Element parent = element.getParent();
                        while (parent != null) {
                            parents.add(parent);
                            parent = parent.getParent();
                        }

                        for (Element p : parents.reversed()) {
                            if (!elementsInWriteOrder.contains(p)) {
                                elementsInWriteOrder.add(p);
                            }
                        }

                        int index = 0;
                        List<Element> siblings = elementsInWriteOrder.stream().filter(e -> e.getCanonicalName().contains(element.getParent().getCanonicalName().substring(element.getParent().getCanonicalName().indexOf("://")))).toList();
                        if (siblings.isEmpty()) {
                            index = elementsInWriteOrder.indexOf(element.getParent()) + 1;
                            elementsInWriteOrder.add(index, element);
                        } else {
                            index = elementsInWriteOrder.indexOf(siblings.getLast()) + 1;
                            elementsInWriteOrder.add(index, element);
                        }
                    }
                }

                Stack<Element> stack = new Stack<>();
                for (Element element : elementsInWriteOrder) {
                    if (elements.contains(element)) {
                        if (stack.isEmpty() || stack.peek() == element.getParent()) {
                            writeElement(view, element, writer);
                        } else {
                            while (!stack.isEmpty() && stack.peek() != element.getParent()) {
                                stack.pop();
                                endBoundary(view, writer);
                            }
                            writeElement(view, element, writer);
                        }
                    } else {
                        while (!stack.isEmpty() && stack.peek() != element.getParent()) {
                            stack.pop();
                            endBoundary(view, writer);
                        }

                        startBoundary(view, element, writer);
                        stack.push(element);
                    }
                }

                while (!stack.isEmpty()) {
                    stack.pop();
                    endBoundary(view, writer);
                }
            } else {
                for (Element element : elements) {
                    writeElement(view, element, writer);
                }
            }

            if (!elements.isEmpty()) {
                writer.writeLine();
            }

            writeRelationships(view, writer);
            writeFooter(view, writer);

            Diagram diagram = createDiagram(view, writer.toString());
            diagram.setLegend(createLegend(view));

            return diagram;
        } else {
            return super.export(view);
        }
    }

    protected abstract void startBoundary(DynamicView view, Element element, IndentingWriter writer);

    void endBoundary(DynamicView view, IndentingWriter writer) {
        writer.outdent();
        writer.writeLine("end box");
    }

    @Override
    protected void writeFooter(ModelView view, IndentingWriter writer) {
        writer.writeLine("@enduml");
    }

    @Override
    protected Diagram createDiagram(ModelView view, String definition) {
        return new PlantUMLDiagram(view, definition);
    }

    protected boolean isSupportedIcon(String icon) {
        return !StringUtils.isNullOrEmpty(icon) && icon.startsWith("http");
    }

    protected double calculateIconScale(String iconUrl, int maxIconSize) {
        double scale = 0.5;

        try {
            URL url = new URL(iconUrl);
            BufferedImage bi = ImageIO.read(url);

            int width = bi.getWidth();
            int height = bi.getHeight();

            scale = ((double)maxIconSize) / Math.max(width, height);
        } catch (UnsupportedOperationException | UnsatisfiedLinkError | IIOException e) {
            // This is a known issue on native builds since AWT packages aren't available.
            // So we just swallow the error and use the default scale
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scale;
    }

}
