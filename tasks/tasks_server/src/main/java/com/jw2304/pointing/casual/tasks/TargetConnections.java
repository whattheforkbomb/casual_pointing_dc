package com.jw2304.pointing.casual.tasks;

import java.io.IOException;
import java.net.ServerSocket;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TargetConnections {
    
    @Bean
    public ServerSocket server(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            
            
            
            
            return serverSocket;
        } catch (IOException ioex) {
            throw new BeanInstantiationException(ServerSocket.class, "Unable to create server socket on port: %d".formatted(port), ioex);
        }
    }

}
