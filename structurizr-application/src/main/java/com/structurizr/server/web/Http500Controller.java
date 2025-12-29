package com.structurizr.server.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class Http500Controller extends AbstractController {

    private static final Log log = LogFactory.getLog(Http500Controller.class);

    @RequestMapping(value = "/500", method = RequestMethod.GET)
    public String show404Page(ModelMap model) {
        addCommonAttributes(model, "500", true);

        Exception exception = (Exception)model.get("SPRING_SECURITY_LAST_EXCEPTION");
        log.error(exception);

        return "500";
    }

    @RequestMapping(value = "/500", method = RequestMethod.POST)
    public String showErrorPage(ModelMap model) {
        addCommonAttributes(model, "500", true);

        Exception exception = (Exception)model.get("SPRING_SECURITY_LAST_EXCEPTION");
        log.error(exception);

        return "500";
    }

}
