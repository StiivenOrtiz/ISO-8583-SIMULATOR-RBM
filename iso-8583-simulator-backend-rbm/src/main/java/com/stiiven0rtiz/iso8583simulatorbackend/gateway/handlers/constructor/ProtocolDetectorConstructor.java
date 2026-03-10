package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.constructor;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.SupportsProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProtocolDetectorConstructor extends SimpleChannelInboundHandler<ProtocolFrame> {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDetectorConstructor.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final Map<ProtocolType, ProtocolFrameConstructor> constructors;

    public ProtocolDetectorConstructor(List<ProtocolFrameConstructor> decoders) {
        this.constructors = decoders.stream()
                .collect(Collectors.toMap(
                        d -> d.getClass()
                                .getAnnotation(SupportsProtocol.class)
                                .value(), Function.identity()
                ));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolFrame protocolFrame) {
        logger.debug("{} - Received: {}. Start detecting protocol...", thisId, protocolFrame);

        ProtocolType detectedProtocol = protocolFrame.protocol();

        logger.debug("{} - Detected Protocol: {}", thisId, detectedProtocol);

        ProtocolFrameConstructor constructor = constructors.get(detectedProtocol);

        if (constructor == null) {
            logger.error("{} - No constructor found for detected protocol: {}", thisId, detectedProtocol);
            throw new IllegalStateException("No decoder for protocol " + detectedProtocol);
        }

        logger.debug("{} - Using constructor: {} for protocol: {}",
                thisId, constructor.getClass().getSimpleName(), detectedProtocol);

        ProtocolFrame constructedMsg;

        try {
            constructedMsg = constructor.constructMSG(protocolFrame);
        } catch (Exception e) {
            logger.error("{} - Fatal error constructing message for protocol {}: {}",
                    thisId, detectedProtocol, e.getMessage(), e);
            throw new RuntimeException("Message construction error for protocol " + detectedProtocol, e);
        }

        if (constructedMsg != null) {
            logger.debug("{} - Constructed message: {}", thisId, constructedMsg);
            ctx.fireChannelRead(constructedMsg);
        } else {
            logger.error("{} - Constructor {} returned null for protocol {}", thisId, constructor.getClass().getSimpleName(), detectedProtocol);
            throw new RuntimeException("Constructor " + constructor.getClass().getSimpleName() + " returned null for protocol " + detectedProtocol);
        }
    }
}
