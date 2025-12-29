package com.structurizr.server.web.workspace;

import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.util.HtmlUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

public abstract class AbstractImageController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(AbstractImageController.class);

    protected Resource getImage(WorkspaceMetaData workspaceMetaData, String branch, String filename, HttpServletResponse response) {
        filename = HtmlUtils.filterHtml(filename);

        try {
            InputStreamAndContentLength inputStreamAndContentLength = workspaceComponent.getImage(workspaceMetaData.getId(), branch, filename + ".png");
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
                log.warn("Error while trying to get image " + filename + ".png from workspace with ID " + workspaceMetaData.getId());
            } else {
                log.error("Error while trying to get image " + filename + ".png from workspace with ID " + workspaceMetaData.getId(), e);
            }

            response.setStatus(404);
            return null;
        }
    }

}