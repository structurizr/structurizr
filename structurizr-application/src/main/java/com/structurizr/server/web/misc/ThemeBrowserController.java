package com.structurizr.server.web.misc;

import com.structurizr.server.web.AbstractController;
import com.structurizr.util.BuiltInThemes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
class ThemeBrowserController extends AbstractController {

    @RequestMapping(value = "/themes", method = RequestMethod.GET)
    String showThemeBrowser(ModelMap model) {
        model.addAttribute("themes", BuiltInThemes.getThemes());

        addCommonAttributes(model, "Theme browser", false);

        return "theme-browser";
    }

}