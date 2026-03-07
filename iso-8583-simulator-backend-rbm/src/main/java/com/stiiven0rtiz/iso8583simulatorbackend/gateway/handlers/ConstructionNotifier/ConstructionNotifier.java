package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.ConstructionNotifier;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.TransactionContext;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ConstructedIso8583Metadata;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;
import com.stiiven0rtiz.iso8583simulatorbackend.services.TransactionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructionNotifier extends SimpleChannelInboundHandler<ProtocolFrame> {
    private static final Logger logger = LoggerFactory.getLogger(ConstructionNotifier.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final TransactionService transactionService;

    public ConstructionNotifier(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolFrame frame) {
        logger.debug("{} - Received constructed message: {}. Start saving construction...", thisId, frame);
        // delegate to executer:

        TransactionContext context = frame.context();

        boolean constructionCompleted = context.getConstructedAt() != null;

        logger.debug("{} - Construction completed: {}, has error: {}, protocol: {}",
                thisId, constructionCompleted, context.hasError(), frame.protocol());

        if (constructionCompleted && !context.hasError() && frame.protocol() != null) {

            logger.debug("{} - Generating construction for protocol: {}", thisId, frame.protocol());

            Promise<Transaction> promise = ctx.executor().newPromise();
            context.setTransaction(promise);

            // log unsupported protocol
            if (frame.protocol() == ProtocolType.ISO8583) {
                logger.info("{} - Generating construction for ISO8583 message: {}", thisId, frame.metadata());

                try {
                    Transaction tx = transactionService.generateConstruction(
                            ((ConstructedIso8583Metadata) frame.metadata()).iso8583Msg(),
                            context.getReceivedAt(),
                            context.getConstructedAt()
                    );
                    logger.info("{} - Generated and notified construction for transaction UUID: {}", thisId, tx.getUuid());
                    promise.setSuccess(tx);
                } catch (Exception e) {
                    logger.error("{} - Error generating construction: {}", thisId, e.getMessage(), e);
                    promise.setFailure(e);
                }

            }
        }

        ctx.fireChannelRead(frame);
    }
}