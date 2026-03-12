package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.persistence;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.POJO.ChannelAttributes;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.TransactionContext;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.services.TransactionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class GlobalErrorHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final TransactionService transactionService;

    public GlobalErrorHandler(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        logger.error("{} - Exception caught in GlobalErrorHandler. Remote address: {}", thisId, ctx.channel().remoteAddress(), cause);

        TransactionContext context =
                ctx.channel()
                        .attr(ChannelAttributes.TX_CONTEXT)
                        .get();

        if (context == null) {
            logWithoutContext(ctx, cause);
            ctx.close();
            return;
        }

        context.setError(cause);

        persistError(context, cause);

        ctx.close();
    }

    private void persistError(TransactionContext context, Throwable cause) {

        if (shouldPersistWithTransactionId(context)) {
            persistUsingTransactionId(context, cause);
            return;
        }

        persistWithoutTransactionId(context);
    }

    private boolean shouldPersistWithTransactionId(TransactionContext context) {
        return (context.getConstructedAt() != null) && (context.getRespondedAt() == null);
    }

    private void persistUsingTransactionId(TransactionContext context, Throwable cause) {

        Promise<Long> promise = context.getTransactionId();
        Promise<Transaction> transactionPromise = context.getTransaction();

        if (promise != null) {
            logger.error("{} - Error occurred after construction but before response. Attempting to persist with transaction ID...", thisId, cause);

            promise.addListener(future -> {
                if (future.isSuccess()) {
                    Long transactionId = (Long) future.getNow();
                    transactionService.saveErrorResponse(transactionId);
                } else {
                    logger.error("{} - Transaction ID promise failed. Persisting without ID.", thisId, cause);
                    persistWithoutTransactionId(context);
                }
            });
        } else if (transactionPromise != null) {
            logger.error("{} - Error occurred after construction but before response. Attempting to persist with transaction UUID...", thisId, cause);

            LocalDateTime constructionCompletedAt = context.getConstructedAt();

            transactionPromise.addListener(future -> {
                if (future.isSuccess()) {
                    logger.debug("{} - Transaction promise completed successfully. Saving response...", thisId);
                    Transaction tx = (Transaction) future.getNow();
                    transactionService.saveErrorT(tx, constructionCompletedAt);
                } else {
                    logger.error("{} - Transaction promise failed. Cannot save response. (Transaction error failed)", thisId, future.cause());
                    persistWithoutTransactionId(context);
                }
            });

        } else {
            logger.warn("{} - Transaction ID or UUID promise is null. Persisting without ID.", thisId);
            persistWithoutTransactionId(context);
        }
    }

    private void persistWithoutTransactionId(TransactionContext context) {

        String rawRequest = context.getRawRequest();
        ProtocolType protocolType = context.getProtocolType();
        LocalDateTime receivedAt = context.getReceivedAt();

        if (rawRequest == null) {
            logger.error("{} - Cannot persist error: raw request is null.", thisId);
            return;
        }

        if (protocolType == null) {
            protocolType = ProtocolType.UNKNOWN;
            logger.warn("{} - Protocol type is null. Defaulting to 'unknown'.", thisId);
        }

        // print discard raw request
        logger.error("{} - Discarding raw request due to error: {}", thisId, rawRequest);

        if (receivedAt != null) // pertsist when message has been decoded
            transactionService.saveError(rawRequest, protocolType, receivedAt);
        else if (protocolType == ProtocolType.UNKNOWN) // persist when message has not been decoded but protocol type is known
            transactionService.saveError(rawRequest, protocolType);
        else // persist when message has not been decoded and protocol type is unknown
            transactionService.saveError(rawRequest);
    }

    private void logWithoutContext(ChannelHandlerContext ctx,
                                   Throwable cause) {

        logger.error(
                "{} - Unhandled exception without transaction context. Remote address: {}",
                thisId,
                ctx.channel().remoteAddress(),
                cause
        );
    }
}
