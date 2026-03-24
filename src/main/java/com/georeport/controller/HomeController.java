package com.georeport.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Redirect root path to the index landing page
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }
}
