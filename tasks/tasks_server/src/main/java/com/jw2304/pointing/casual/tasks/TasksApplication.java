package com.jw2304.pointing.casual.tasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
// @EnableWebMvc
public class TasksApplication implements WebMvcConfigurer {

	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("*")
            .allowedOriginPatterns("*")
            .allowedHeaders("*")
            .allowedMethods("*")
            .allowCredentials(false);
    }

	public static void main(String[] args) {
		SpringApplication.run(TasksApplication.class, args);
	}

}
