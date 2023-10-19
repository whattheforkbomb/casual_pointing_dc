package com.jw2304.pointing.casual.tasks.connections;

import org.springframework.web.socket.WebSocketSession;

public interface WebSocketConnectionConsumer {
    public String getSocketRegistrationPath();

    public void connectionRegistered(WebSocketSession session);
}
