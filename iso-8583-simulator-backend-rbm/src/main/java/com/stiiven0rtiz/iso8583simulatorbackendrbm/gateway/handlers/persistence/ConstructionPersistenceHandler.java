package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.persistence;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.TransactionContext;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ConstructedIso8583Metadata;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.services.TransactionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructionPersistenceHandler extends SimpleChannelInboundHandler<ProtocolFrame> {
    private static final Logger logger = LoggerFactory.getLogger(ConstructionPersistenceHandler.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final TransactionService transactionService;

    public ConstructionPersistenceHandler(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolFrame frame) {
        logger.debug("{} - Received constructed message: {}. Start saving construction...", thisId, frame);
        // delegate to executer:

        TransactionContext context = frame.context();

        boolean isConstructionCompleted = context.getConstructedAt() != null;

        logger.debug("{} - Construction completed: {}, has error: {}, protocol: {}",
                thisId, isConstructionCompleted, context.hasError(), frame.protocol());

        if (isConstructionCompleted && !context.hasError() && frame.protocol() != null) {

            logger.debug("{} - Saving construction for protocol: {}", thisId, frame.protocol());

            Promise<Long> promise = ctx.executor().newPromise();
            context.setTransactionId(promise);

            // log unsupported protocol
            if (frame.protocol() == ProtocolType.ISO8583) {
                logger.info("{} - Saving construction for ISO 8583 message", thisId);

                ctx.executor().execute(() -> {
                    try {
                        Long id = transactionService.saveISOConstruction(
                                ((ConstructedIso8583Metadata) frame.metadata()).iso8583Msg(),
                                context.getReceivedAt(),
                                context.getConstructedAt()
                        );
                        logger.info("{} - Construction ISO8583 message saved with id: {}", thisId, id);
                        promise.setSuccess(id);
                    } catch (Exception e) {
                        logger.error("{} - Error saving construction for ISO 8583 message", thisId, e);
                        promise.setFailure(e);
                    }
                });
            }
        }

        ctx.fireChannelRead(frame);
    }
}