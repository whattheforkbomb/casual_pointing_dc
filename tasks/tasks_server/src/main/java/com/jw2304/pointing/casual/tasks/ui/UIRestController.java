package com.jw2304.pointing.casual.tasks.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
@EnableWebMvc
@Controller
public class UIRestController implements WebMvcConfigurer {

    public static Logger LOG = LoggerFactory.getLogger(UIRestController.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
          .addResourceHandler("/app/**")
          .addResourceLocations("classpath:/static/");
          // .resourceChain(true)
          // .addResolver(new EncodedResourceResolver())
          // .addResolver(new PathResourceResolver());
        LOG.info("Adding UI to resource handler.");
    }

    // @GetMapping("favicon.ico")
    // @ResponseBody
    // void returnNoFavicon() {
    // }
}