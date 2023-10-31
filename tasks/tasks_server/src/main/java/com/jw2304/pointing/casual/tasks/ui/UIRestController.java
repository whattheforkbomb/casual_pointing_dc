package com.jw2304.pointing.casual.tasks.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

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
    //     registry.addViewController("/app/questionnaire/").setViewName("/app/questionnaire/index.html");
    //     registry.addViewController("/app/stroop/").setViewName("forward:/app/stroop/index.html");
    //     registry.addViewController("/app/stroop2/").setViewName("forward:/stroop/index.html");
    //     registry.addViewController("/app/stroop3/").setViewName("forward:stroop/index.html");
    //     registry.addViewController("/app/stroop4/").setViewName("forward:app/stroop/index.html");
    // }

    @GetMapping("/app/{site}/")
    public String redirect(@PathVariable("site") String site) {
        return "redirect:/app/" + site + "/index.html";
    }

    @GetMapping("favicon.ico")
    @ResponseBody
    public void returnNoFavicon() {}
}