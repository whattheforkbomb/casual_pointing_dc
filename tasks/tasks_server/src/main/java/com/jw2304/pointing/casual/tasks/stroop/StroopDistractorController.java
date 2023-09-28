package com.jw2304.pointing.casual.tasks.stroop;

import java.net.http.WebSocket;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class StroopDistractorController implements WebSocketConfigurer {
    public static Logger LOG = LoggerFactory.getLogger(StroopDistractorController.class);

    @Bean
    public StroopWebSocketHandler wsHandler() {
        return new StroopWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        LOG.info("Registering websocket handler");
        registry.addHandler(wsHandler(), "/stroop");
    }

}
