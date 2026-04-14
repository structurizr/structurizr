package com.structurizr.dsl;

import com.structurizr.documentation.Documentable;
import com.structurizr.importer.documentation.DefaultImageImporter;
import com.structurizr.importer.documentation.DocumentationImporter;
import com.structurizr.util.StringUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class DocumentationDslContext extends DslContext {

    private final Documentable documentable;
    private final File dslFile;
    private String path;
    private String fullyQualifiedClassName;

    private final List<String> excludes = new ArrayList<>();

    DocumentationDslContext(Documentable documentable, File dslFile) {
        this.documentable = documentable;
        this.dslFile = dslFile;
    }

    void setFullyQualifiedClassName(String fullyQualifiedClassName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    void setPath(String path) {
        this.path = path;
    }

    void exclude(String regex) {
        if (!StringUtils.isNullOrEmpty(regex)) {
            excludes.add(regex);
        }
    }

    Set<String> getExcludes() {
        return new HashSet<>(excludes);
    }

    @Override
    void end() {
        if (dslFile != null) {
            File docsPath = new File(dslFile.getParentFile(), path);

            try {
                Class<?> clazz = loadClass(fullyQualifiedClassName, dslFile);
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                DocumentationImporter documentationImporter = (DocumentationImporter)constructor.newInstance();
                for (String exclude : excludes) {
                    documentationImporter.exclude(exclude);
                }

                documentationImporter.importDocumentation(documentable, docsPath);

                if (docsPath.isDirectory()) {
                    DefaultImageImporter imageImporter = new DefaultImageImporter();
                    imageImporter.importDocumentation(documentable, docsPath);
                }
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException("Error importing documentation: " + fullyQualifiedClassName + " was not found");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error importing documentation: " + e.getMessage());
            }
        }
    }

    @Override
    protected String[] getPermittedTokens() {
        return new String[] {
                StructurizrDslTokens.DOCS_EXCLUDE
        };
    }

}