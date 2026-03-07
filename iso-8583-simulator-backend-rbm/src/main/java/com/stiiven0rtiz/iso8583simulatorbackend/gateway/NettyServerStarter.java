package com.stiiven0rtiz.iso8583simulatorbackend.gateway;


// imports
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * NettyServerStarter.java
 *
 * This class is responsible for starting the Netty server when the Spring Boot application starts.
 *
 * @version 1.1
 */
@Component
public class NettyServerStarter implements CommandLineRunner {

    private final NettyServerController serverController;

    public NettyServerStarter(NettyServerController serverController) {
        this.serverController = serverController;
    }

    @Override
    public void run(String... args) {
        new Thread(() -> {
            try {
                serverController.startNetty();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "netty-server-thread").start();
    }
}
