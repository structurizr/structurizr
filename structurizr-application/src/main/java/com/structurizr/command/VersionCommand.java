package com.structurizr.command;

import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.util.Version;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VersionCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(VersionCommand.class);

    public VersionCommand() {
    }

    public void run(String... args) throws Exception {
        log.info("structurizr: " + new Version().getBuildNumber());

        try {
            log.info("structurizr-*: " + Class.forName(StructurizrDslParser.class.getCanonicalName()).getPackage().getImplementationVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Java: " + System.getProperty("java.version") + "/"  + System.getProperty("java.vendor") + " (" + System.getProperty("java.home") + ")");
        log.info("OS: " + System.getProperty("os.name") + " "  + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");
    }

}