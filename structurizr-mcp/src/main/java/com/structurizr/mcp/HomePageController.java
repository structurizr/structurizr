package com.structurizr.mcp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomePageController {

    @RequestMapping("/")
    public String homePage() {
        return "redirect:https://docs.structurizr.com/ai/mcp";
    }

}