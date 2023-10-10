package com.jw2304.pointing.casual.tasks.stroop;

import java.net.http.WebSocket;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableWebMvc
public class StroopDistractorController implements WebSocketConfigurer, WebMvcConfigurer {
    public static Logger LOG = LoggerFactory.getLogger(StroopDistractorController.class);

    @Bean
    public StroopWebSocketHandler wsHandler() {
        return new StroopWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        LOG.info("Registering websocket handler");
        registry.addHandler(wsHandler(), "/stroop").setAllowedOriginPatterns("*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        LOG.info("Registering CORS Mapping");
        registry.addMapping("/*")
            .allowedOriginPatterns("*")
            .allowedHeaders("*")
            .allowedMethods("*")
            .allowCredentials(false);
    }

}
