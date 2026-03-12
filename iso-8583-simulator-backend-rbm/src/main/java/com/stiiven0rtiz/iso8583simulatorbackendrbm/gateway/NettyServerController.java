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
 * @version 1.2
 */
@Component
public class NettyServerController {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerController.class);

    private final ServerBootstrap bootstrap;
    private final ChannelGroup allChannels;
    private final int port;

    private ChannelFuture channelFuture;

    public NettyServerController(ServerBootstrap bootstrap, ChannelGroup allChannels, int port) {
        this.bootstrap = bootstrap;
        this.allChannels = allChannels;
        this.port = port;
    }

    public synchronized void startNetty() throws InterruptedException {
        logger.info("Starting Netty on port {}", port);
        channelFuture = bootstrap.bind(port).sync();
    }

    public synchronized void stopNetty() throws InterruptedException {
        logger.info("Stopping Netty...");

        if (allChannels != null)
            allChannels.close().sync();

        if (channelFuture != null && channelFuture.channel().isOpen())
            channelFuture.channel().close().sync();

        logger.info("Netty stopped.");
    }

    public void restartNetty() {
        new Thread(() -> {
            try {
                stopNetty();
                startNetty();
                logger.info("Netty restarted successfully!");
            } catch (Exception e) {
                logger.error("Error restarting Netty", e);
            }
        }, "netty-restart-thread").start();
    }
}
