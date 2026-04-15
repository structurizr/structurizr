package com.structurizr.importer.documentation;

import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Documentable;
import com.structurizr.documentation.Image;
import com.structurizr.documentation.Section;
import com.structurizr.util.ImageUtils;
import com.structurizr.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Scans a given directory and imports all images into the workspace. The "reluctant" property can be
 * used to configure which images are imported:
 * <ul>
 *     <li>true: only import images that are referenced in documentation/decisions</li>
 *     <li>false: import all images (default behaviour)</li>
 * </ul>
 */
public class DefaultImageImporter implements DocumentationImporter {

    private boolean reluctant = false;

    /**
     * Creates a new instance, that will import all images it finds.
     */
    public DefaultImageImporter() {
        this(false);
    }

    /**
     * Creates a new instance.
     *
     * @param reluctant     only import images that are referenced in documentation/decisions when true,
     *                      import all images otherwise
     */
    public DefaultImageImporter(boolean reluctant) {
        this.reluctant = reluctant;
    }

    /**
     * Imports one or more png/jpg/jpeg/gif images from the specified path.
     *
     * @param documentable      the item that documentation should be associated with
     * @param path              the path to import images from
     */
    public void importDocumentation(Documentable documentable, File path) {
        if (documentable == null) {
            throw new IllegalArgumentException("A workspace, software system, container, or component must be specified.");
        }

        if (path == null) {
            throw new IllegalArgumentException("A path must be specified.");
        } else if (!path.exists()) {
            throw new IllegalArgumentException(path.getAbsolutePath() + " does not exist.");
        }

        Set<String> imagesInDocumentation = new HashSet<>();
        if (reluctant) {
            for (Section section : documentable.getDocumentation().getSections()) {
                imagesInDocumentation.addAll(section.findImages());
            }

            for (Decision decision : documentable.getDocumentation().getDecisions()) {
                imagesInDocumentation.addAll(decision.findImages());
            }
        }

        try {
            if (path.isDirectory()) {
                importImages(documentable, "", path, imagesInDocumentation);
            } else {
                importImage(documentable, "", path, imagesInDocumentation);
            }
        } catch (Exception e) {
            throw new DocumentationImportException(e.getMessage(), e);
        }
    }

    private void importImages(Documentable documentable, String root, File path, Set<String> imagesInDocumentation) throws IOException {
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName().toLowerCase();
                if (file.isDirectory() && !file.isHidden()) {
                    if (StringUtils.isNullOrEmpty(root)) {
                        importImages(documentable, file.getName(), file, imagesInDocumentation);
                    } else {
                        importImages(documentable, root + "/" + file.getName(), file, imagesInDocumentation);
                    }
                } else {
                    if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif") || name.endsWith(".svg")) {
                        importImage(documentable, root, file, imagesInDocumentation);
                    }
                }
            }
        }
    }

    private void importImage(Documentable documentable, String path, File file, Set<String> imagesInDocumentation) throws IOException {
        String contentType = ImageUtils.getContentType(file);
        String base64Content;

        String name;
        if (StringUtils.isNullOrEmpty(path)) {
            name = file.getName();
        } else {
            name = path + "/" + file.getName();
        }

        if (ImageUtils.CONTENT_TYPE_IMAGE_SVG.equalsIgnoreCase(contentType)) {
            base64Content = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        } else {
            contentType = ImageUtils.getContentType(file);
            base64Content = ImageUtils.getImageAsBase64(file);
        }

        if (!reluctant || imagesInDocumentation.contains(name)) {
            documentable.getDocumentation().addImage(new Image(name, contentType, base64Content));
        }
    }

}