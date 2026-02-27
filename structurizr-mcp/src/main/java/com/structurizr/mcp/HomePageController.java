package com.structurizr.mcp;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomePageController implements ErrorController {

    @RequestMapping("/")
    public String homePage() {
        return "redirect:https://docs.structurizr.com/mcp-server";
    }

}