package com.structurizr.mcp;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.export.AbstractDiagramExporter;
import com.structurizr.export.Diagram;
import com.structurizr.view.ModelView;
import com.structurizr.view.View;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

abstract class AbstractExportTools extends AbstractTools {

    private static final Log log = LogFactory.getLog(AbstractExportTools.class);

    Diagram export(String dsl, String viewKey, AbstractDiagramExporter diagramExporter) {
        try {
            StructurizrDslParser parser = createStructurizrDslParser();
            parser.parse(dsl);

            Workspace workspace = parser.getWorkspace();
            View view = workspace.getViews().getViewWithKey(viewKey);

            if (view instanceof ModelView) {
                return diagramExporter.export((ModelView)view);
            } else {
                throw new RuntimeException("The view " + viewKey + " is not a model view");
            }
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

}