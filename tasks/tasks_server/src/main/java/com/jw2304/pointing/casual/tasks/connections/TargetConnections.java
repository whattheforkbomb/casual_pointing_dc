package com.jw2304.pointing.casual.tasks.connections;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TargetConnections {

    public static Logger LOG = LoggerFactory.getLogger(TargetConnections.class);

    @Value("${targets.expected.connections}")
    public int expectedConnections;

    @Bean
    public ArrayList<Socket> targetSockets() {
        return new ArrayList<>(expectedConnections);
    } 

    @Bean
    public ExecutorService executor() {
        return Executors.newFixedThreadPool(expectedConnections+2);
    }

    @Bean
    public ServerSocket server(@Value("${targets.port}") int port) {
        try {
            LOG.debug("Creating socket on port: %d".formatted(port));
            return new ServerSocket(port);
        } catch (IOException ioex) {
            throw new BeanInstantiationException(ServerSocket.class, "Unable to create server socket on port: %d".formatted(port), ioex);
        }
    }

    @Bean
	public CommandLineRunner acceptTargetConnections(ExecutorService executor, ServerSocket server, ArrayList<Socket> targetSockets) throws Exception {
		return args -> {
            executor.execute(() -> {
                while (true) {
                    LOG.info("Accepting incoming connections from targets");
                    try {
                        Socket socket = server.accept();
                        LOG.info("New Target Connection Established: %s".formatted(socket.getInetAddress().getHostAddress()));
                        socket.setKeepAlive(true);
                        targetSockets.add(socket);
                    } catch (IOException ioex) {
                        LOG.error("Connection Failed To Be Accepted", ioex);
                    }
                }
            });
        };
	}


}
