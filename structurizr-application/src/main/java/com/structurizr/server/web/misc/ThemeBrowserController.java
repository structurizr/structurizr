package com.structurizr.server.web.misc;

import com.structurizr.server.web.AbstractController;
import com.structurizr.view.InstalledThemes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
class ThemeBrowserController extends AbstractController {

    @RequestMapping(value = "/themes", method = RequestMethod.GET)
    String showThemeBrowser(ModelMap model) {
        List<String> themes = new ArrayList<>(InstalledThemes.getThemes());
        Collections.sort(themes);
        model.addAttribute("themes", themes);

        addCommonAttributes(model, "Theme browser", false);

        return "theme-browser";
    }

}