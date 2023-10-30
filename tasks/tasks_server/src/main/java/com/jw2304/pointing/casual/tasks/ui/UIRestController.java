package com.jw2304.pointing.casual.tasks.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
// import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
// @EnableWebMvc
@Controller
public class UIRestController { // implements WebMvcConfigurer {

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

    @GetMapping("/app/{site}2/")
    public String redirect2(@PathVariable("site") String site) {
        return "redirect:app/" + site + "/index.html";
    }

    @GetMapping("/app/{site}3/")
    public String redirect3(@PathVariable("site") String site) {
        return "forward:app/" + site + "/index.html";
    }

     @GetMapping("/static/{site}/")
    public String redirect4(@PathVariable("site") String site) {
        return "forward:/static/ " + site + "/index.html";
    }

    @GetMapping("/{site}/")
    public String redirect5(@PathVariable("site") String site) {
        return "redirect:/" + site + "/index.html";
    }

    @GetMapping("/{site}2/")
    public String redirect6(@PathVariable("site") String site) {
        return "forward:/static/" + site + "/index.html";
    }

    @GetMapping("/{site}3/")
    public String redirect7(@PathVariable("site") String site) {
        return "forward:/" + site + "/index.html";
    }

    @GetMapping("/{site}4/")
    public String redirect8(@PathVariable("site") String site) {
        return "forward:static/" + site + "/index.html";
    }

    @GetMapping("/{site}5/")
    public String redirect9(@PathVariable("site") String site) {
        return "forward:" + site + "/index.html";
    }

    @GetMapping("favicon.ico")
    @ResponseBody
    public void returnNoFavicon() {}
}