package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.response.deprecated;


// imports

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ConstructedIso8583Metadata;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.TransactionAttributes;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.config.IsoFieldsData;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.message.Iso8583Msg;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.ArtificialDelayDetect;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.ISOResponseLoader;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.ResponseCodeLoader;
import com.stiiven0rtiz.iso8583simulatorbackend.services.TransactionService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.stiiven0rtiz.iso8583simulatorbackend.utils.IsoUtils.iso8584MSGToHEXString;

/**
 * This handler processes incoming ISO 8583 messages and constructs appropriate responses.
 *
 * @version 1.3
 */
@Deprecated
public class ISO8583ResponseHandler extends SimpleChannelInboundHandler<ProtocolFrame> {
    private static final Logger logger = LoggerFactory.getLogger(ISO8583ResponseHandler.class);
    String thisId = toString().substring(toString().indexOf("@"));
    ISOResponseLoader isoResponseLoader;
    IsoFieldsData isoFieldsData;
    ArtificialDelayDetect artificialDelayDetect;
    ResponseCodeLoader responseCodeLoader;
    TransactionService transactionService;

    public ISO8583ResponseHandler(IsoFieldsData isoFieldsData, ISOResponseLoader isoResponseLoader, ArtificialDelayDetect artificialDelayDetect, ResponseCodeLoader responseCodeLoader, TransactionService transactionService) {
        this.isoFieldsData = isoFieldsData;
        this.isoResponseLoader = isoResponseLoader;
        this.artificialDelayDetect = artificialDelayDetect;
        this.responseCodeLoader = responseCodeLoader;
        this.transactionService = transactionService;
    }

    /**
     * Handles the incoming ISO 8583 message, constructs a response, and sends it back to the client.
     *
     * @param ctx the channel handler context
     * @param msg the incoming ISO 8583 message
     * @throws Exception if an error occurs during message processing
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolFrame msg) throws Exception {
        ConstructedIso8583Metadata metadata = (ConstructedIso8583Metadata) msg.metadata();
        logger.debug("{} - Received ISO 8583 MSG.", thisId);
        Iso8583Msg isoC = metadata.iso8583Msg();

        try {
            Iso8583Msg responseMsg = isoResponseLoader.buildResponse(isoC);
            responseCodeLoader.validateResponseCode(responseMsg);

            responseMsg.setRawData(iso8584MSGToHEXString(isoFieldsData, responseMsg).replace(" ", ""));

            String terminalId = (String) isoC.getDataElement("P41");
            int delay = artificialDelayDetect.getDelay(terminalId);

            // Apply the delay and display a log message per second
            if (delay > 0) {
                for (int i = 0; i < delay; i++) {
                    int secondsRemaining = delay - i;

                    // Schedule a log message for each second
                    ctx.executor().schedule(() -> {
                        logger.info("{} - Delaying response to {} ... {} seconds remaining", thisId, terminalId, secondsRemaining);
                    }, i, TimeUnit.SECONDS);
                }
                ctx.executor().schedule(() -> sendResponse(ctx, responseMsg), delay, TimeUnit.SECONDS);
            } else
                sendResponse(ctx, responseMsg);

            saveResponse(ctx, delay, responseMsg);
        } catch (Exception e) {
            logger.error("{} - Error constructing response: {}", thisId, e.getMessage());
            logger.error("{} - Discarded message (HEX): {}", thisId, isoC.getRawData());

            ctx.executor().submit(() -> {
                transactionService.saveError(isoC.getRawData());
            });

            logger.error("{} - Closing connection...", thisId);
            e.printStackTrace();
            ctx.close();
        }
    }

    /**
     * Sends the response message back to the client.
     *
     * @param ctx         the channel handler context
     * @param responseMsg the response ISO 8583 message
     */
    private void sendResponse(ChannelHandlerContext ctx, Iso8583Msg responseMsg) {
        String hex = responseMsg.getRawData().replace(" ", "");
        int len = hex.length();
        byte[] bytes = new byte[len / 2];

        // Convert HEX string to byte array
        for (int i = 0; i < len; i += 2)
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));

        ByteBuf buffer = ctx.alloc().buffer(bytes.length);
        buffer.writeBytes(bytes);

        logger.info("{} - Sending response (HEX): {}", thisId, hex);
        ctx.writeAndFlush(buffer);
    }

    /**
     * Saves the response message along with the artificial delay.
     *
     * @param ctx         the channel handler context
     * @param delay       the artificial delay applied
     * @param responseMsg the response ISO 8583 message
     */
    private void saveResponse(ChannelHandlerContext ctx, int delay, Iso8583Msg responseMsg) {
        ctx.executor().submit(() -> {
            try {
                Promise<Long> promise = ctx.channel().attr(TransactionAttributes.ATTR_TX_ID).get();

                if (promise == null) {
                    logger.error("Missing transaction ID promise.");
                    return;
                }

                promise.addListener(f -> {
                    if (f.isSuccess()) {
                        Long id = promise.getNow();
                        transactionService.saveISOResponse(id, delay, responseMsg, LocalDateTime.now(), LocalDateTime.now());
                    } else
                        logger.error("Error resolving construction ID", f.cause());
                });

            } catch (Exception e) {
                logger.error("{} - ERROR in saving transaction: {}", thisId, e.getMessage());
            }
        });
    }
}
