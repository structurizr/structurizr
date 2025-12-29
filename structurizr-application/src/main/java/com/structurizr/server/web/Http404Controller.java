package com.structurizr.server.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class Http404Controller extends AbstractController {

    @RequestMapping(value = "/404", method = RequestMethod.GET)
    public String show404Page(ModelMap model) {
        addCommonAttributes(model, "404", true);

        return "404";
    }

}
