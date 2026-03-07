package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.response;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.TransactionContext;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ResponseMetadata;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.SupportsProtocol;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.BytesParser;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.HexParser;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.ArtificialDelayDetect;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProtocolDetectorResponse extends SimpleChannelInboundHandler<ProtocolFrame> {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDetectorResponse.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final Map<ProtocolType, ProtocolResponse> responders;
    private final ArtificialDelayDetect artificialDelayDetect;

    public ProtocolDetectorResponse(List<ProtocolResponse> decoders, ArtificialDelayDetect artificialDelayDetect) {
        this.responders = decoders.stream()
                .collect(Collectors.toMap(
                        d -> d.getClass()
                                .getAnnotation(SupportsProtocol.class)
                                .value(), Function.identity()
                ));
        this.artificialDelayDetect = artificialDelayDetect;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolFrame protocolFrame) throws Exception {
        logger.debug("{} - Received: {}. Start detecting protocol...", thisId, protocolFrame);

        ProtocolType detectedProtocol = protocolFrame.protocol();

        logger.debug("{} - Detected Protocol: {}", thisId, detectedProtocol);

        ProtocolResponse constructor = responders.get(detectedProtocol);

        if (constructor == null)
            throw new IllegalStateException("No decoder for protocol " + detectedProtocol);

        logger.debug("{} - Using constructor: {} for protocol: {}",
                thisId, constructor.getClass().getSimpleName(), detectedProtocol);

        ProtocolFrame responseMsg;

        try {
            responseMsg = constructor.response(protocolFrame);
        } catch (Exception e) {
            logger.error("{} - Fatal error constructing message for protocol {}: {}", thisId, detectedProtocol, e.getMessage(), e);
            throw new RuntimeException("Message construction error for protocol " + detectedProtocol, e);
        }


        if (responseMsg == null) {
            logger.error("{} - Constructed message is null for protocol {}", thisId, detectedProtocol);
            throw new IllegalStateException("Constructed message is null for protocol " + detectedProtocol);
        }

        ResponseMetadata responseMetadata = (ResponseMetadata) responseMsg.metadata();
        TransactionContext context = responseMsg.context();
        ProtocolFrame responseProtocolFrame = new ProtocolFrame(detectedProtocol, responseMetadata, context);

        if (responseMetadata.getTerminalId() != null) {
            String terminalId = responseMetadata.getTerminalId();

            int delay = artificialDelayDetect.getDelay(terminalId);

            if (delay <= 0) {
                responseMetadata.setArtificialDelay(0);
                sendResponse(ctx, responseProtocolFrame);
                return;
            }

            responseMetadata.setArtificialDelay(delay);

            for (int i = 0; i < delay; i++) {
                int secondsRemaining = delay - i;

                ctx.executor().schedule(() -> {
                    logger.info("{} - Delaying response to {} ... {} seconds remaining",
                            thisId, terminalId, secondsRemaining);
                }, i, TimeUnit.SECONDS);
            }

            ctx.executor().schedule(() -> sendResponse(ctx, responseProtocolFrame),
                    delay, TimeUnit.SECONDS);

            return;
        }

        logger.warn("{} - Terminal ID is null for response message: {}", thisId, responseMsg);
        responseMetadata.setArtificialDelay(0);

        sendResponse(ctx, responseProtocolFrame);
    }

    private void sendResponse(ChannelHandlerContext ctx, ProtocolFrame protocolFrame) {
        ResponseMetadata responseMetadata = (ResponseMetadata) protocolFrame.metadata();
        TransactionContext context = protocolFrame.context();

        String hex = HexParser.parseHexWithoutSpaces(responseMetadata.getRawData());
        byte[] bytes = BytesParser.parseHexWithoutSpacesString(hex);
        ByteBuf buffer = ctx.alloc().buffer(bytes.length);
        buffer.writeBytes(bytes);

        logger.info("{} - Sending response (HEX): {}", thisId, hex);

        ctx.writeAndFlush(buffer);
        context.setResponded();

        ctx.fireChannelRead(protocolFrame);
    }

}
