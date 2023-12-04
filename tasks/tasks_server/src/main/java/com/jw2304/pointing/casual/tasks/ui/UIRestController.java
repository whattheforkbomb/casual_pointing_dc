package com.jw2304.pointing.casual.tasks.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
// import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

@Controller
public class UIRestController {
    public static Logger LOG = LoggerFactory.getLogger(UIRestController.class);

    // @Override
    // public void addResourceHandlers(ResourceHandlerRegistry registry) {
    //     registry
    //       .addResourceHandler("/app/**")
    //       .addResourceLocations("classpath:/static/");
    //     LOG.info("Adding UI to resource handler.");
    // }

    // @Override
    // public void addViewControllers(ViewControllerRegistry registry) {
    //     registry.addViewController("/app/questionnaire/").setViewName("redirect:/app/questionnaire/index.html");
    //     registry.addViewController("/app/stroop/").setViewName("redirect:/app/stroop/index.html");
    // }

    @GetMapping("/app/{site}")
    public String redirect(@PathVariable("site") String site) {
        return "redirect:/app/" + site + "/index.html";
    }

    @GetMapping("/app/{site}/")
    public String redirectSlash(@PathVariable("site") String site) {
        return "redirect:/app/" + site + "/index.html";
    }

    @GetMapping("favicon.ico")
    @ResponseBody
    public void returnNoFavicon() {}
}