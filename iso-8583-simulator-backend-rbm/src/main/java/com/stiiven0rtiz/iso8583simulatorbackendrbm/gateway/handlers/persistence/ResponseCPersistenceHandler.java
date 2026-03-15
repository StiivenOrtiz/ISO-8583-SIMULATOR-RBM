package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.persistence;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.TransactionContext;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.*;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.services.TransactionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseCPersistenceHandler extends SimpleChannelInboundHandler<ProtocolFrame> {
    private static final Logger logger = LoggerFactory.getLogger(ResponseCPersistenceHandler.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final TransactionService transactionService;

    public ResponseCPersistenceHandler(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolFrame frame) {

        logger.debug("{} - Received response message: {}. Start saving response...", thisId, frame);

        TransactionContext context = frame.context().copyAndDestroy();

        Promise<Transaction> promise = context.getTransaction();

        if (promise == null) {
            logger.error("{} - No transaction found in context. Cannot save response. (Missing transaction promise)", thisId);
            return;
        }

        promise.addListener(future -> {
            if (future.isSuccess()) {

                logger.debug("{} - Transaction promise completed successfully. Saving response...", thisId);

                Transaction tx = (Transaction) future.getNow();
                ProtocolType protocol = frame.protocol();

                if (protocol == ProtocolType.ISO8583) {
                    ctx.executor().execute(() -> {
                        try {
                            ResponseMetadata responseMetadata = (ResponseMetadata) frame.metadata();

                            transactionService.saveISOResponse(
                                    tx,
                                    responseMetadata.getArtificialDelay(),
                                    ((ResponseMSGIso8583) responseMetadata.getResponseMSG()).responseMsg(),
                                    context.getRespondedAt(),
                                    context.getProcessedAt()
                            );

                            logger.info("{} - ISO8583 Response for transaction UUID: {} saved successfully.", thisId, tx.getUuid());

                        } catch (Exception e) {
                            logger.error("{} - Error saving ISO8583 response for transaction UUID: {}", thisId, tx.getUuid(), e);
                        }
                    });
                } else if (protocol == ProtocolType.HTTP) {
                    ctx.executor().execute(() -> {
                        try {
                            ResponseMetadata responseMetadata = (ResponseMetadata) frame.metadata();

                            transactionService.saveHTTPResponse(
                                    tx,
                                    ((ResponseMSGHTTP) responseMetadata.getResponseMSG()).transaction(),
                                    responseMetadata.getArtificialDelay(),
                                    responseMetadata.getRawData(),
                                    context.getRespondedAt(),
                                    context.getProcessedAt()
                            );

                            logger.info("{} - HTTP Response for transaction UUID: {} saved successfully.", thisId, tx.getUuid());

                        } catch (Exception e) {
                            logger.error("{} - Error saving HTTP response for transaction UUID: {}", thisId, tx.getUuid(), e);
                        }
                    });
                } else
                    logger.error("{} - Transaction promise failed. Cannot save response. (Transaction construction failed)", thisId, future.cause());
            }
        });
    }
}
