package com.structurizr.documentation;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentationContentTests {

    @Test
    void findImages_ReturnsAnEmptySet_WhenThereAreNoImages() {
        DocumentationContent content = new Section(Format.Markdown, "");
        assertTrue(content.findImages().isEmpty());
    }

    @Test
    void findImages_Markdown() {
        DocumentationContent content = new Section(Format.Markdown, """
                # Heading
                
                ![alt](image1.png)

                ![](image2.png)
                """);

        Set<String> images = content.findImages();
        assertEquals(2, images.size());
        assertTrue(images.contains("image1.png"));
        assertTrue(images.contains("image2.png"));
    }

    @Test
    void findImages_AsciiDoc() {
        DocumentationContent content = new Section(Format.AsciiDoc, """
                = Heading
                
                image::image1.png[alt]

                image:image2.png[alt]
                """);

        Set<String> images = content.findImages();
        assertEquals(2, images.size());
        assertTrue(images.contains("image1.png"));
        assertTrue(images.contains("image2.png"));
    }

}
