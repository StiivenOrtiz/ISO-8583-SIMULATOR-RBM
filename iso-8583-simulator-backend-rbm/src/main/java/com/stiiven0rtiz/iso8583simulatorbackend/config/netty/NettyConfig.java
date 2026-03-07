package com.stiiven0rtiz.iso8583simulatorbackend.config.netty;

// imports

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.NettyServerController;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.ConstructionNotifier.ConstructionNotifier;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.constructor.Iso8583FrameConstructor;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.constructor.ProtocolDetectorConstructor;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.decoder.Iso8583Decoder;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.decoder.ProtocolDetectorDecoder;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.persistence.ConstructionPersistenceHandler;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.persistence.GlobalErrorHandler;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.persistence.ResponseCPersistenceHandler;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.persistence.ResponsePersistenceHandler;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.response.Iso8583Response;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.response.ProtocolDetectorResponse;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.config.IsoFieldsData;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.ArtificialDelayDetect;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.ISOResponseLoader;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.ResponseCodeLoader;
import com.stiiven0rtiz.iso8583simulatorbackend.services.TransactionService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * NettyConfig.java
 * <p>
 * This class configures the Netty server, including the event loop groups,
 * server bootstrap, and channel initializer.
 *
 * @version 1.3
 */
@Configuration
public class NettyConfig {
    private static final Logger logger = LoggerFactory.getLogger(NettyConfig.class);

    /**
     * The port on which the Netty server will listen for incoming connections.
     */
    @Value("${netty.port}")
    private int port;

    /**
     * The number of threads to use for the boss group and worker group.
     */
    @Value("${netty.bosses.threads}")
    private int bossThreads;

    /**
     * The number of threads to use for the worker group.
     */
    @Value("${netty.workers.threads}")
    private int workerThreads;

    /**
     * The number of threads to use for the response group.
     */
    @Value("${netty.response.threads}")
    private int responseThreads;

    @Value("${netty.constructor.threads}")
    private int buildersThreads;

    @Value("${netty.frame.threads}")
    private int framersThreads;

    @Value("${netty.persistence.threads}")
    private int persistenceThreads;

    @Value("${netty.decoder.maxErrors}")
    private int decoderMaxErrors;

    @Value("${netty.close.timeout.hours}")
    private int closeTimeoutHours;

    /**
     * The number of threads to use for the boss group.
     */
    @Bean(destroyMethod = "shutdownGracefully")
    public EventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossThreads);
    }

    /**
     * The number of threads to use for the worker group.
     */
    @Bean(destroyMethod = "shutdownGracefully")
    public EventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerThreads);
    }

    /**
     * The executor group for handling framing of ISO8583 messages.
     */
    @Bean(destroyMethod = "shutdownGracefully")
    public EventExecutorGroup executorFramerGroup() {
        return new DefaultEventExecutorGroup(framersThreads);
    }

    /**
     * The executor group for constructing ISO8583 message receives.
     */
    @Bean(destroyMethod = "shutdownGracefully")
    public EventExecutorGroup executorConstructorGroup() {
        return new DefaultEventExecutorGroup(buildersThreads);
    }

    /**
     * The executor group for handling ISO8583 responses.
     */
    @Bean(destroyMethod = "shutdownGracefully")
    public EventExecutorGroup executorResponseGroup() {
        return new DefaultEventExecutorGroup(responseThreads);
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public EventExecutorGroup executorPersistenceGroup() {
        return new DefaultEventExecutorGroup(persistenceThreads);
    }


    /**
     * The server bootstrap configuration for the Netty server.
     *
     * @param bossGroup          The boss group for accepting incoming connections.
     * @param workerGroup        The worker group for processing incoming connections.
     * @param channelInitializer The channel initializer for setting up the pipeline.
     * @return The configured ServerBootstrap instance.
     */
    @Bean
    public ServerBootstrap serverBootstrap(EventLoopGroup bossGroup,
                                           EventLoopGroup workerGroup,
                                           ChannelInitializer<SocketChannel> channelInitializer) {
        return new ServerBootstrap().group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer);
    }

    /**
     * The channel group for managing all active channels.
     *
     * @return The ChannelGroup instance.
     */
    @Bean
    public ChannelGroup allChannels() {
        return new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    /**
     * The channel initializer for the Netty server, which sets up the pipeline
     * for handling incoming connections.
     *
     * @return The configured ChannelInitializer instance.
     */
    @Bean
    public ChannelInitializer<SocketChannel> channelInitializer(
            TransactionService transactionService,
            IsoFieldsData isoFieldsData,
            ISOResponseLoader isoResponseLoader,
            ArtificialDelayDetect artificialDelayDetect,
            ResponseCodeLoader responseCodeLoader,
            EventExecutorGroup executorFramerGroup,
            EventExecutorGroup executorConstructorGroup,
            EventExecutorGroup executorResponseGroup,
            EventExecutorGroup executorPersistenceGroup,
            ChannelGroup allChannels) {

        // Create a new ChannelInitializer for SocketChannel
        return new ChannelInitializer<>() {
            /**
             * Initializes the channel by adding the custom server handler to the pipeline.
             *
             * @param ch The SocketChannel to initialize.
             */
            @Override
            protected void initChannel(SocketChannel ch) {
                allChannels.add(ch);

//                ch.pipeline().addLast(executorFramerGroup, new Iso8583FrameDecoder(decoderMaxErrors, isoFieldsData, transactionService));

                ch.pipeline().addLast(executorFramerGroup,
                        new ProtocolDetectorDecoder(List.of(
                                new Iso8583Decoder(isoFieldsData)),
                                decoderMaxErrors));

//                ch.pipeline().addLast(executorConstructorGroup,
//                        new Iso8583MSGConstructorHandler(isoFieldsData, transactionService));

                ch.pipeline().addLast(executorConstructorGroup,
                        new ProtocolDetectorConstructor(List.of(
                                new Iso8583FrameConstructor(isoFieldsData))));

//                ch.pipeline().addLast(executorPersistenceGroup,
//                        new ConstructionPersistenceHandler(transactionService));

                ch.pipeline().addLast(executorPersistenceGroup,
                        new ConstructionNotifier(transactionService));

//                ch.pipeline().addLast(executorResponseGroup,
//                        new ISO8583ResponseHandler(isoFieldsData, isoResponseLoader, artificialDelayDetect, responseCodeLoader, transactionService));

                ch.pipeline().addLast(executorResponseGroup,
                        new ProtocolDetectorResponse(List.of(
                                new Iso8583Response(isoFieldsData, isoResponseLoader, responseCodeLoader)
                        ), artificialDelayDetect));

//                ch.pipeline().addLast(executorPersistenceGroup,
//                        new ResponsePersistenceHandler(transactionService));

                ch.pipeline().addLast(executorResponseGroup,
                        new ResponseCPersistenceHandler(transactionService));

                ch.pipeline().addLast(executorPersistenceGroup,
                        new GlobalErrorHandler(transactionService));

                ch.eventLoop().schedule(() -> {
                    if (ch.isOpen()) {
                        logger.info("Closing connection by refresh connection timeout, channelId={}", ch.id());
                        ch.close();
                    }
                }, closeTimeoutHours, TimeUnit.HOURS);
            }
        };
    }

    @Bean
    public NettyServerController nettyServerController(ServerBootstrap serverBootstrap, ChannelGroup allChannels, @Value("${netty.port}") int port) {
        return new NettyServerController(serverBootstrap, allChannels, port);
    }


//    /**
//     * The Netty server starter, which binds the server to the specified port
//     * and starts listening for incoming connections.
//     *
//     * @param serverBootstrap The server bootstrap for configuring the server.
//     * @return The NettyServerStarter instance.
//     */
//    @Bean
//    public NettyServerStarter nettyServerStarter(ServerBootstrap serverBootstrap) {
//        return new NettyServerStarter(serverBootstrap, port);
//    }
}