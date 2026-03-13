package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * NettyServerController.java
 *
 * This class is responsible for controlling the lifecycle of the Netty server,
 * including starting, stopping, and restarting the server.
 *
 * This class only closes and re-binds the server channel. The event loop groups remain active.
 *
 * @version 1.3
 */
@Component
public class NettyServerController {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerController.class);

    private final ServerBootstrap bootstrap;
    private final ChannelGroup allChannels;
    private final int port;

    private volatile ChannelFuture channelFuture;

    public NettyServerController(ServerBootstrap bootstrap, ChannelGroup allChannels, int port) {
        this.bootstrap = bootstrap;
        this.allChannels = allChannels;
        this.port = port;
    }

    /**
     * Starts Netty only if it is not already running.
     */
    public synchronized void startNetty() throws InterruptedException {

        if (isRunning()) {
            logger.warn("Netty already running on port {}", port);
            return;
        }

        logger.info("Starting Netty on port {}", port);

        channelFuture = bootstrap.bind(port).sync();

        logger.info("Netty started successfully.");
    }

    /**
     * Stops Netty server channel and closes all active connections.
     */
    public synchronized void stopNetty() throws InterruptedException {

        if (!isRunning()) {
            logger.warn("Netty is not running.");
            return;
        }

        logger.info("Stopping Netty...");

        if (allChannels != null && !allChannels.isEmpty()) {
            logger.info("Closing {} active connections...", allChannels.size());
            allChannels.close().sync();
        }

        if (channelFuture != null) {
            channelFuture.channel().close().sync();
        }

        channelFuture = null;

        logger.info("Netty stopped.");
    }

    /**
     * Restart Netty in a safe synchronized way.
     */
    public synchronized void restartNetty() {

        try {

            logger.info("Restarting Netty...");

            stopNetty();
            startNetty();

            logger.info("Netty restarted successfully.");

        } catch (Exception e) {

            logger.error("Error restarting Netty", e);

        }
    }

    /**
     * True if server socket is active and accepting connections.
     */
    public synchronized boolean isRunning() {

        return channelFuture != null
                && channelFuture.channel() != null
                && channelFuture.channel().isActive();
    }

    /**
     * Returns number of active TCP connections.
     */
    public int activeConnections() {
        return allChannels != null ? allChannels.size() : 0;
    }

}