package com.structurizr.dsl.plugin.documentation;

import com.structurizr.Workspace;
import com.structurizr.documentation.Documentable;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.Section;
import com.structurizr.dsl.StructurizrDslPluginContext;
import com.structurizr.importer.diagrams.plantuml.PlantUMLImporter;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.SoftwareSystem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlantUMLTests {

    private static final String MARKDOWN_CONTENT = """
                ## Context
                
                ```plantuml
                @startuml
                Bob-> Alice : hello1
                @enduml
                ```
                
                ```plantuml
                @startuml
                Bob-> Alice : hello2
                @enduml
                ```
                
                More text...""";

    private static final String ASCIIDOC_CONTENT = """
                == Context
                
                [plantuml]
                ....
                @startuml
                Bob-> Alice : hello1
                @enduml
                ....
                
                [plantuml, target=output-file-name, format=output-format]
                ....
                @startuml
                Bob-> Alice : hello2
                @enduml
                ....
                
                More text...""";

    @Test
    void markdown() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_URL_PROPERTY, "https://www.plantuml.com/plantuml");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Name");
        Container container = softwareSystem.addContainer("Name");
        Component component = container.addComponent("Name");

        List<Documentable> documentables = new ArrayList<>();

        workspace.getDocumentation().addSection(new Section(Format.Markdown, MARKDOWN_CONTENT));
        documentables.add(workspace);

        softwareSystem.getDocumentation().addSection(new Section(Format.Markdown, MARKDOWN_CONTENT));
        documentables.add(softwareSystem);

        container.getDocumentation().addSection(new Section(Format.Markdown, MARKDOWN_CONTENT));
        documentables.add(container);

        component.getDocumentation().addSection(new Section(Format.Markdown, MARKDOWN_CONTENT.replace("\\n", "\r\n"))); // Windows CRLF
        documentables.add(component);

        Map<String,String> parameters = new HashMap<>();
        StructurizrDslPluginContext context = new StructurizrDslPluginContext(null, null, workspace, parameters);
        new PlantUML().run(context);

        for (Documentable documentable : documentables) {
            for (Section section : documentable.getDocumentation().getSections()) {
                assertEquals("""
## Context

![](https://www.plantuml.com/plantuml/svg/SoWkIImgAStDuNBAJzArKt3CoKnELR1Io4ZDoSatv798pKi1IG80)

![](https://www.plantuml.com/plantuml/svg/SoWkIImgAStDuNBAJzArKt3CoKnELR1Io4ZDoSatud98pKi1IG80)

More text...""", section.getContent());
            }
        }
    }

    @Test
    void asciidoc() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_URL_PROPERTY, "https://www.plantuml.com/plantuml");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Name");
        Container container = softwareSystem.addContainer("Name");
        Component component = container.addComponent("Name");

        List<Documentable> documentables = new ArrayList<>();

        workspace.getDocumentation().addSection(new Section(Format.AsciiDoc, ASCIIDOC_CONTENT));
        documentables.add(workspace);

        softwareSystem.getDocumentation().addSection(new Section(Format.AsciiDoc, ASCIIDOC_CONTENT));
        documentables.add(softwareSystem);

        container.getDocumentation().addSection(new Section(Format.AsciiDoc, ASCIIDOC_CONTENT));
        documentables.add(container);

        component.getDocumentation().addSection(new Section(Format.AsciiDoc, ASCIIDOC_CONTENT.replace("\\n", "\r\n"))); // Windows CRLF
        documentables.add(component);

        Map<String,String> parameters = new HashMap<>();
        StructurizrDslPluginContext context = new StructurizrDslPluginContext(null, null, workspace, parameters);
        new PlantUML().run(context);

        for (Documentable documentable : documentables) {
            for (Section section : documentable.getDocumentation().getSections()) {
                assertEquals("""
== Context

image::https://www.plantuml.com/plantuml/svg/SoWkIImgAStDuNBAJzArKt3CoKnELR1Io4ZDoSatv798pKi1IG80[]

image::https://www.plantuml.com/plantuml/svg/SoWkIImgAStDuNBAJzArKt3CoKnELR1Io4ZDoSatud98pKi1IG80[]

More text...""", section.getContent());
            }
        }
    }

}