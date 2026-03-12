package com.stiiven0rtiz.iso8583simulatorbackendrbm.config.websocket;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.websocket.handler.TxWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocketConfig.java
 *
 * Configuration class to set up WebSocket endpoints.
 *
 * @version 1.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TxWebSocketHandler handler;

    public WebSocketConfig(TxWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/tx")
                .setAllowedOrigins("*");
    }
}
