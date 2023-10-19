package com.jw2304.pointing.casual.tasks.connections;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketRegistration implements WebSocketConfigurer {
    public static Logger LOG = LoggerFactory.getLogger(WebSocketRegistration.class);

    @Autowired
    public List<WebSocketConnectionConsumer> websocketConsumers;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        for (WebSocketConnectionConsumer handler : websocketConsumers) {
            LOG.info("Registering websocket handler");
            registry.addHandler(new AbstractWebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    handler.connectionRegistered(session);
                }
            }, "/%s".formatted(handler.getSocketRegistrationPath())).setAllowedOriginPatterns("*");
        }
    }

}
