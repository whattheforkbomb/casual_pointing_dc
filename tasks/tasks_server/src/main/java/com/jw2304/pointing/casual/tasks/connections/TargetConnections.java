package com.jw2304.pointing.casual.tasks.connections;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
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

    private static final int MAC_LEN = 6;

    public static Logger LOG = LoggerFactory.getLogger(TargetConnections.class);

    @Value("${targets.expected.connections}")
    public int expectedConnections;

    @Bean
    public Map<String, Socket> targetSockets() {
        return new HashMap<>(expectedConnections);
    } 

    @Bean
    public List<String> targetSocketIds() {
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
	public CommandLineRunner acceptTargetConnections(ExecutorService executor, ServerSocket server, Map<String, Socket> targetSockets, List<String> targetSocketIds) throws Exception {
		return args -> {
            executor.execute(() -> {
                while (true) {
                    LOG.info("Accepting incoming connections from targets");
                    try {
                        Socket socket = server.accept();
                        String ipAddress = socket.getInetAddress().getHostAddress();
                        String MACStr = null;

                        LOG.info("New Target Connection Established: %s".formatted(ipAddress));
                        InputStream in = socket.getInputStream();
                        byte[] socketMAC = new byte[MAC_LEN];
                        int availableBytes = in.available();
                        int bytesRead = 0;
                        if (availableBytes > 0) {
                            bytesRead = in.read(socketMAC);
                            if (bytesRead > 0) {
                                HexFormat socketMACHex = HexFormat.of();
                                MACStr = socketMACHex.formatHex(socketMAC);
                                LOG.info("Socket MAC sent: %s - %d".formatted(MACStr, bytesRead));
                            }
                        }
                        String address = ipAddress;
                        LOG.info("Socket ID: %s".formatted(address));
                        socket.setKeepAlive(true);
                        targetSockets.put(address, socket);
                        if (targetSocketIds.stream().filter(address::equals).findFirst().isEmpty())
                            targetSocketIds.add(address);
                    } catch (IOException ioex) {
                        LOG.error("Connection Failed To Be Accepted", ioex);
                    } catch (Exception pkmn) {
                        LOG.error("Who knows...", pkmn);
                    }
                }
            });
        };
	}

}
