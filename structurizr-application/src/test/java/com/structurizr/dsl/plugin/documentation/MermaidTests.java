package com.structurizr.dsl.plugin.documentation;

import com.structurizr.Workspace;
import com.structurizr.documentation.Documentable;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.Section;
import com.structurizr.dsl.StructurizrDslPluginContext;
import com.structurizr.importer.diagrams.mermaid.MermaidImporter;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.SoftwareSystem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MermaidTests {

    private static final String MARKDOWN_CONTENT = """
                ## Context
                
                ```mermaid
                flowchart TD
                    Start1 --> Stop1
                ```
                
                ```mermaid
                flowchart TD
                    Start2 --> Stop2
                ```
                
                ```mermaid
                stateDiagram-v2
                    [*] --> Still
                    Still --> [*]
                    Still --> Moving
                    Moving --> Still
                    Moving --> Crash
                    Crash --> [*]
                ```
                
                More text...""";

    private static final String ASCIIDOC_CONTENT = """
                == Context
                
                [mermaid]
                ....
                flowchart TD
                    Start1 --> Stop1
                ....
                
                [mermaid, target=output-file-name, format=output-format]
                ....
                flowchart TD
                    Start2 --> Stop2
                ....
                
                [mermaid]
                ....
                stateDiagram-v2
                    [*] --> Still
                    Still --> [*]
                    Still --> Moving
                    Moving --> Still
                    Moving --> Crash
                    Crash --> [*]
                ....
                
                More text...""";

    @Test
    void markdown() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(MermaidImporter.MERMAID_URL_PROPERTY, "https://mermaid.ink");
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
        new Mermaid().run(context);

        for (Documentable documentable : documentables) {
            for (Section section : documentable.getDocumentation().getSections()) {
                assertEquals("""
                        ## Context
                        
                        ![](https://mermaid.ink/svg/pako:eJyrVlBKzk9JVbJSSsvJL0_OSCwqUQhxiclTAILgEiDPUEFX1w7IzC8wjMlT0lFQyk0tyk3MTFGyqlYqyUjNBWlNSU1LLM0pUaqtBQDc5xlV)
            
                        ![](https://mermaid.ink/svg/pako:eJyrVlBKzk9JVbJSSsvJL0_OSCwqUQhxiclTAILgEiDPSEFX1w7IzC8wislT0lFQyk0tyk3MTFGyqlYqyUjNBWlNSU1LLM0pUaqtBQDdORlX)
            
                        ![](https://mermaid.ink/svg/pako:eJyrVlBKzk9JVbJSKi5JLEl1yUxML0rM1S0zislTAIJorVgFXV07heCSzJwciBCYCRYESqIL-eaXZealQ0QhbHTtSKLORYnFGRBRMBNhqJKOglJualFuYmaKklW1UklGai7IiSmpaYmlOSVKtbUAgeo3Cg==)
                        
                        More text...""", section.getContent());
            }
        }
    }

    @Test
    void asciidoc() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(MermaidImporter.MERMAID_URL_PROPERTY, "https://mermaid.ink");
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
        new Mermaid().run(context);

        for (Documentable documentable : documentables) {
            for (Section section : documentable.getDocumentation().getSections()) {
                assertEquals("""
== Context

image::https://mermaid.ink/svg/pako:eJyrVlBKzk9JVbJSSsvJL0_OSCwqUQhxiclTAILgEiDPUEFX1w7IzC8wjMlT0lFQyk0tyk3MTFGyqlYqyUjNBWlNSU1LLM0pUaqtBQDc5xlV[]

image::https://mermaid.ink/svg/pako:eJyrVlBKzk9JVbJSSsvJL0_OSCwqUQhxiclTAILgEiDPSEFX1w7IzC8wislT0lFQyk0tyk3MTFGyqlYqyUjNBWlNSU1LLM0pUaqtBQDdORlX[]

image::https://mermaid.ink/svg/pako:eJyrVlBKzk9JVbJSKi5JLEl1yUxML0rM1S0zislTAIJorVgFXV07heCSzJwciBCYCRYESqIL-eaXZealQ0QhbHTtSKLORYnFGRBRMBNhqJKOglJualFuYmaKklW1UklGai7IiSmpaYmlOSVKtbUAgeo3Cg==[]

More text...""", section.getContent());
            }
        }
    }

}