package com.structurizr.command;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.http.HttpClient;
import com.structurizr.inspection.DefaultInspector;
import com.structurizr.util.BuiltInThemes;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.validation.WorkspaceScopeValidatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;

public abstract class AbstractCommand {

    private static final Log log = LogFactory.getLog(AbstractCommand.class);

    private static final String PLUGINS_DIRECTORY_NAME = "plugins";

    protected AbstractCommand() {
    }

    public abstract void run(String... args) throws Exception;

    String getAgent() {
        return "structurizr/" + getClass().getPackage().getImplementationVersion();

    }

    protected Workspace loadWorkspace(String workspacePathAsString) throws Exception {
        Workspace workspace;

        if (workspacePathAsString.endsWith("json")) {
            if (workspacePathAsString.startsWith("http://") || workspacePathAsString.startsWith("https")) {
                String json = readFromUrl(workspacePathAsString);
                workspace = WorkspaceUtils.fromJson(json);
            } else {
                File workspaceFile = new File(workspacePathAsString);
                if (!workspaceFile.exists()) {
                    throw new StructurizrException(workspaceFile.getAbsolutePath() + " does not exist");
                }

                if (!workspaceFile.isFile()) {
                    throw new StructurizrException(workspaceFile.getAbsolutePath() + " is not a JSON or DSL file");
                }

                workspace = WorkspaceUtils.loadWorkspaceFromJson(workspaceFile);
            }

        } else {
            StructurizrDslParser structurizrDslParser = new StructurizrDslParser();
            structurizrDslParser.getHttpClient().allow(".*");
            structurizrDslParser.setCharacterEncoding(Charset.defaultCharset());

            if (workspacePathAsString.startsWith("http://") || workspacePathAsString.startsWith("https://")) {
                String dsl = readFromUrl(workspacePathAsString);
                structurizrDslParser.parse(dsl);
            } else {
                File workspaceFile = new File(workspacePathAsString);
                if (!workspaceFile.exists()) {
                    throw new StructurizrException(workspaceFile.getAbsolutePath() + " does not exist");
                }

                if (!workspaceFile.isFile()) {
                    throw new StructurizrException(workspaceFile.getAbsolutePath() + " is not a JSON or DSL file");
                }

                structurizrDslParser.parse(workspaceFile);
            }

            workspace = structurizrDslParser.getWorkspace();

            if (workspace == null) {
                throw new StructurizrException("No workspace definition was found - please check your DSL");
            }
        }

        // validate workspace scope
        WorkspaceScopeValidatorFactory.getValidator(workspace).validate(workspace);

        // run default inspections
        new DefaultInspector(workspace);

        // inline built-in theme icons
        BuiltInThemes.inlineIcons(workspace);

        // add default views if no views are explicitly defined
        if (!workspace.getModel().isEmpty() && workspace.getViews().isEmpty()) {
            log.info(" - no views defined; creating default views");
            workspace.getViews().createDefaultViews();
        }

        return workspace;
    }

    protected String readFromUrl(String url) {
        HttpClient httpClient = new HttpClient();
        httpClient.allow(".*");
        return httpClient.get(url).getContentAsString();
    }

    protected Class loadClass(String fqn, File workspaceFile) throws Exception {
        File pluginsDirectory = new File(workspaceFile.getParent(), PLUGINS_DIRECTORY_NAME);
        URL[] urls = new URL[0];

        if (pluginsDirectory.exists()) {
            File[] jarFiles = pluginsDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles != null) {
                urls = new URL[jarFiles.length];
                for (int i = 0; i < jarFiles.length; i++) {
                    System.out.println(jarFiles[i].getAbsolutePath());
                    try {
                        urls[i] = jarFiles[i].toURI().toURL();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        URLClassLoader childClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
        return childClassLoader.loadClass(fqn);
    }

    protected void configureDebugLogging() {
        Logger root = (Logger) LoggerFactory.getLogger("com.structurizr");
        root.setLevel(Level.DEBUG);
    }

}