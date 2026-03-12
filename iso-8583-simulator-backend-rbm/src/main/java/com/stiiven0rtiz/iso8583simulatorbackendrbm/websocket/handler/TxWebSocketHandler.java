package com.stiiven0rtiz.iso8583simulatorbackendrbm.websocket.handler;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TxWebSocketHandler.java
 *
 * WebSocket handler to manage transaction live feed connections.
 *
 * @version 1.1
 */
@Component
public class TxWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(TxWebSocketHandler.class);

    /**
     * Active WebSocket sessions indexed by session ID.
     * Using Map avoids issues when wrapping sessions with decorators.
     */
    @Getter
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * Called when a new WebSocket connection is established.
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {

        WebSocketSession concurrentSession =
                new ConcurrentWebSocketSessionDecorator(
                        session,
                        2000,           // 2s timeout
                        256 * 1024      // 256KB buffer
                );

        sessions.put(session.getId(), concurrentSession);

        logger.info("Client connected to Live Feed: {}", session.getId());
    }

    /**
     * Called when a WebSocket connection is closed.
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull CloseStatus status) {

        sessions.remove(session.getId());

        logger.info("Client disconnected from Live Feed: {} (status: {})",
                session.getId(), status);
    }

    /**
     * Called when a transport error occurs.
     */
    @Override
    public void handleTransportError(@NonNull WebSocketSession session,
                                     @NonNull Throwable exception) {

        logger.error("Transport error in session {}",
                session.getId(), exception);

        sessions.remove(session.getId());

        try {
            if (session.isOpen()) {
                session.close(CloseStatus.SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error("Error closing session {}", session.getId(), e);
        }
    }

    /**
     * Exposes active sessions for broadcasting.
     */
    public Collection<WebSocketSession> getActiveSessions() {
        return sessions.values();
    }
}