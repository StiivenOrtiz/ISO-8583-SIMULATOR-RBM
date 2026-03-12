package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.persistence;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.TransactionContext;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ResponseMSGIso8583;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ResponseMetadata;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.services.TransactionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponsePersistenceHandler extends SimpleChannelInboundHandler<ProtocolFrame> {
    private static final Logger logger = LoggerFactory.getLogger(ResponsePersistenceHandler.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final TransactionService transactionService;

    public ResponsePersistenceHandler(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolFrame frame) {

        logger.debug("{} - Received response message: {}. Start saving response...", thisId, frame);

        TransactionContext context = frame.context();

        Promise<Long> promise = context.getTransactionId();

        if (promise == null) {
            logger.error("{} - No transaction ID found in context. Cannot save response. (Missing transaction promise)", thisId);
            return;
        }

        promise.addListener(future -> {
            if (future.isSuccess()) {

                logger.debug("{} - Transaction ID promise completed successfully. Saving response...", thisId);

                Long id = (Long) future.getNow();

                ProtocolType protocol = frame.protocol();

                if (protocol == ProtocolType.ISO8583) {
                    ctx.executor().execute(() -> {
                        try {
                            ResponseMetadata responseMetadata = (ResponseMetadata) frame.metadata();

                            transactionService.saveISOResponse(
                                    id,
                                    responseMetadata.getArtificialDelay(),
                                    ((ResponseMSGIso8583) responseMetadata.getResponseMSG()).responseMsg(),
                                    context.getRespondedAt(),
                                    context.getProcessedAt()
                            );

                            logger.info("{} - Response for transaction ID: {} saved successfully.", thisId, id);

                        } catch (Exception e) {
                            logger.error("{} - Error saving response for transaction ID: {}", thisId, id, e);
                        }
                    });
                }
            } else
                logger.error("{} - Transaction ID promise failed. Cannot save response. (Transaction construction failed)", thisId, future.cause());
        });
    }
}
