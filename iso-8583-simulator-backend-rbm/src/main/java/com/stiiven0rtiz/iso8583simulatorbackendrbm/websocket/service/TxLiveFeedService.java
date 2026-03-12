package com.stiiven0rtiz.iso8583simulatorbackendrbm.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.dto.TransactionDto;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.websocket.handler.TxWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * TxLiveFeedService.java
 * <p>
 * Service to broadcast live transaction data to connected WebSocket clients.
 *
 * @version 1.1
 */
@Service
public class TxLiveFeedService {
    private static final Logger logger = LoggerFactory.getLogger(TxLiveFeedService.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final TxWebSocketHandler handler;
    private final ObjectMapper objectMapper;

    private static final int MAX_QUEUE_SIZE = 1000;

    private final Deque<String> buffer = new ArrayDeque<>();

    private final Object lock = new Object();

    public TxLiveFeedService(TxWebSocketHandler handler,
                             ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    public void broadcast(TransactionDto tx) {

        try {
            String json = objectMapper.writeValueAsString(tx);

            synchronized (lock) {
                if (buffer.size() >= MAX_QUEUE_SIZE) {
                    buffer.pollFirst(); // elimina el más viejo
                }
                buffer.addLast(json);
            }

            send(json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void send(String payload) {
        if (payload == null) return;
        TextMessage message = new TextMessage(payload);

        for (WebSocketSession session :
                handler.getActiveSessions()) {

            if (!session.isOpen()) continue;

            try {
                session.sendMessage(message);
            } catch (Exception e) {
                logger.error("{} - Error sending message to session {}: {}",
                        thisId, session.getId(), e.getMessage(), e);
            }
        }
    }
}

