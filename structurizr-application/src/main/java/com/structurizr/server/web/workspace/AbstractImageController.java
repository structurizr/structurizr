package com.structurizr.server.web.workspace;

import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.util.HtmlUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

public abstract class AbstractImageController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(AbstractImageController.class);

    protected Resource getImage(WorkspaceMetadata workspaceMetadata, String branch, String filename, HttpServletResponse response) {
        filename = HtmlUtils.filterHtml(filename);

        try {
            InputStreamAndContentLength inputStreamAndContentLength = workspaceComponent.getImage(workspaceMetadata.getId(), branch, filename);
            if (inputStreamAndContentLength != null) {
                response.setStatus(200);
                return new InputStreamResource(inputStreamAndContentLength.getInputStream()) {
                    @Override
                    public long contentLength() {
                        return inputStreamAndContentLength.getContentLength();
                    }
                };
            } else {
                response.setStatus(404);
                return null;
            }
        } catch (Exception e) {
            if (filename.endsWith("thumbnail")) {
                log.warn("Error while trying to get image " + filename + " from workspace with ID " + workspaceMetadata.getId());
            } else {
                log.error("Error while trying to get image " + filename + " from workspace with ID " + workspaceMetadata.getId(), e);
            }

            response.setStatus(404);
            return null;
        }
    }

}