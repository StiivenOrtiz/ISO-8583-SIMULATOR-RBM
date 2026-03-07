package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.constructor.deprecated;


// imports

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.POJO.ConstructedMessage;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.POJO.MSGLengths;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.DecodedIso8583Metadata;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.TransactionAttributes;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.config.IsoFieldsData;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.message.Iso8583Msg;
import com.stiiven0rtiz.iso8583simulatorbackend.services.TransactionService;
import com.stiiven0rtiz.iso8583simulatorbackend.utils.IsoUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import static com.stiiven0rtiz.iso8583simulatorbackend.utils.IsoUtils.toHex;
import static com.stiiven0rtiz.iso8583simulatorbackend.utils.IsoUtils.toHexBString;

/**
 * This handler constructs an ISO 8583 message from the incoming framed message.
 * It extracts the TPDU, MTI, Bitmap, and Data Elements based on the provided ISO field definitions.
 * The constructed message is logged for debugging purposes.
 * The constructed message is send to the next handler in the pipeline.
 *
 * @version 1.3
 */
@Deprecated
//@Component
public class Iso8583MSGConstructorHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Iso8583MSGConstructorHandler.class);
    String thisId = toString().substring(toString().indexOf("@"));
    private final IsoFieldsData iso;
    private final TransactionService transactionService;

    public Iso8583MSGConstructorHandler(IsoFieldsData iso, TransactionService transactionService) {
        this.iso = iso;
        this.transactionService = transactionService;
    }

    /*
     * This method is called when a message is received.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object input) {
        try {

            ProtocolFrame msg = (ProtocolFrame) input;
            DecodedIso8583Metadata iso8583Metadata;

            try {
                iso8583Metadata = (DecodedIso8583Metadata) msg.metadata();
            } catch (Exception e) {
                logger.error("{} - Error retrieving ISO8583 metadata: {}", thisId, e.getMessage());
                throw new Exception("ISO8583 Metadata retrieval error", e);
            }

            ByteBuf msgCopy = iso8583Metadata.message().copy();

            //make a copy of msg

            Iso8583Msg isoMsg = new Iso8583Msg();
            int bytesRead = 0;

            isoMsg.setRawData(toHex(msgCopy, bytesRead, msgCopy.readableBytes()).replace(" ", ""));

            isoMsg.setTPDU(toHex(msgCopy, bytesRead, iso.getTPDU().length()));
            bytesRead += iso.getTPDU().length();

            isoMsg.setMTI(toHex(msgCopy, iso.getTPDU().length(), iso.getMTI().length()));
            bytesRead += iso.getMTI().length();

            isoMsg.setBitmap(toHex(msgCopy, bytesRead, iso.getBITMAP().length()));
            bytesRead += iso.getBITMAP().length();

            logger.info("{} - Received message: {}", thisId, isoMsg.getRawData());
            logger.info("{} - Received TPDU: {}", thisId, isoMsg.getTPDU().getValue());
            logger.info("{} - Received MTI: {}", thisId, isoMsg.getMTI().getValue());
            logger.info("{} - Received Bitmap: {}", thisId, isoMsg.getBitmap().getValue());

            Map<String, MSGLengths> dataElementsLengths = new TreeMap<>(
                    Comparator.comparingInt(s -> Integer.parseInt(s.substring(1)))
            );
//            dataElementsLengths.putAll(iso8583Metadata.dataElementsLengths());

            for (String fieldId : dataElementsLengths.keySet()) {
                String fieldLengthType = iso.getDataElementById(fieldId).lengthType();
                String fieldType = iso.getDataElementById(fieldId).type();
                int fieldLengthL = 0;

                MSGLengths lengths = dataElementsLengths.get(fieldId);
                int totalLength = lengths.getTotalLength();
                int contentLength = lengths.getContentLength();

                if (!fieldLengthType.equals("F")) { // If the field is not fixed length
                    if ("LLVAR".equals(fieldLengthType)) fieldLengthL = 1;
                    else if ("LLLVAR".equals(fieldLengthType)) fieldLengthL = 2;
                } else {
                    contentLength = iso.getDataElementById(fieldId).length(); // If fixed length, content length is the same as total length
                }

                // Supose the field length is 2 bytes for LLVAR and 3 bytes for LLLVAR
                byte[] fieldValueBytes = new byte[totalLength - fieldLengthL];
                msgCopy.getBytes(msgCopy.readerIndex() + fieldLengthL + bytesRead, fieldValueBytes);

                String fieldValue = toHexBString(fieldValueBytes);

                logger.debug("{} - Field {}: {} - {} , FIELD {}", thisId, fieldId, (totalLength - fieldLengthL), fieldValue, fieldType);
                String postFieldValue = IsoUtils.getValueStringByType(fieldType, fieldId, fieldValueBytes, contentLength, iso);
                logger.info("{} - Post-processed Field [{}]: {}", thisId, fieldId, postFieldValue);

                isoMsg.setDataElement(fieldId, postFieldValue);

                bytesRead += totalLength;
            }

            Promise<Long> promise = ctx.executor().newPromise();
            ctx.channel().attr(TransactionAttributes.ATTR_TX_ID).set(promise);
            ctx.executor().submit(() -> {
                try {
                    Long id = transactionService.saveISOConstruction(isoMsg, LocalDateTime.now(), LocalDateTime.now());
                    promise.setSuccess(id);
                } catch (Exception e) {
                    promise.setFailure(e);
                }
            });

            ctx.fireChannelRead(new ConstructedMessage(isoMsg));
            logger.debug("{} - Constructed message.", thisId);
        } catch (Exception e) {
            logger.error("{} - Error constructing ISO 8583 message: {}", thisId, e.getMessage());

            try {
                ProtocolFrame msg = (ProtocolFrame) input;
                DecodedIso8583Metadata iso8583Metadata;
                iso8583Metadata = (DecodedIso8583Metadata) msg.metadata();
                ByteBuf msgCopy = iso8583Metadata.message().copy();
                String rawMessage = toHex(msgCopy, 0, msgCopy.readableBytes()).replace(" ", "");
                logger.error("{} - Discarded message (HEX): {}", thisId, rawMessage);
                ctx.executor().submit(() -> {
                    transactionService.saveError(rawMessage, ProtocolType.ISO8583);
                });
            } catch (Exception ex) {
                logger.error("{} - Error reading raw data for logging: {}", thisId, ex.getMessage());
            }

            logger.error("{} - Closing connection...", thisId);
            e.printStackTrace();
            ctx.close();
        }
    }
}
