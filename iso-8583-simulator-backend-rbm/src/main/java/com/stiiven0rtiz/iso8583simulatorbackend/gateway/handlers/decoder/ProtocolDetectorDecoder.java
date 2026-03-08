package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.decoder;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.POJO.ChannelAttributes;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.TransactionContext;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.SupportsProtocol;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.config.IsoFieldsData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.BytesParser.bytesToHex;


public class ProtocolDetectorDecoder extends ByteToMessageDecoder {

    String thisId = toString().substring(toString().indexOf("@"));
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDetectorDecoder.class);

    private final Map<ProtocolType, ProtocolFrameDecoder> decoders;
    private ProtocolFrameDecoder activeDecoder = null;
    private int invalidBytesCount = 0;
    private final int maxInvalidBytes;
    private final int TPDU_LENGTH;

    public ProtocolDetectorDecoder(List<ProtocolFrameDecoder> decoders, int maxInvalidBytes, int TPDU_LENGTH) {
        this.decoders = decoders.stream()
                .collect(Collectors.toMap(
                        d -> d.getClass()
                                .getAnnotation(SupportsProtocol.class)
                                .value(), Function.identity()
                ));
        this.maxInvalidBytes = maxInvalidBytes;
        this.TPDU_LENGTH = TPDU_LENGTH;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("{} - New connection established from: {}.", thisId, ctx.channel().remoteAddress());
        ctx.fireChannelActive();
    }

    /**
     * Called when the channel becomes inactive, indicating that the connection has been lost.
     *
     * @param ctx The ChannelHandlerContext for this handler.
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("{} - Connection lost from: {}.", thisId, ctx.channel().remoteAddress());
        ctx.fireChannelInactive();
    }

    /**
     * Called when an exception is caught during the decoding process.
     * This method logs the error and closes the channel.
     *
     * @param ctx   The ChannelHandlerContext for this handler.
     * @param cause The Throwable that was caught.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.info("{} - Connection failed from: {}.", thisId, ctx.channel().remoteAddress());
        ctx.fireExceptionCaught(cause);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        logger.info("{} - Decoding incoming data from: {}.", thisId, ctx.channel().remoteAddress());
        if (!in.isReadable()) return;
        logger.info("{} - Data is readable from: {}.", thisId, ctx.channel().remoteAddress());

        if (activeDecoder == null) {
            logger.info("{} - Decoder is null.", thisId);

            if (in.readableBytes() < TPDU_LENGTH + 2)
                return; // wait for more data

            logger.info("{} - Starting decoding.", thisId);

            in.markReaderIndex();

            byte[] headerBytes = new byte[TPDU_LENGTH + 2];
            in.readBytes(headerBytes);

            // separate TPDU and protocol ID
            byte[] protocodBytes = Arrays.copyOfRange(headerBytes, TPDU_LENGTH, TPDU_LENGTH + 2);

            logger.info("{} - Protocol bytes (HEX): {}, Protocol bytes (ASCII): {}.", thisId, bytesToHex(protocodBytes), new String(protocodBytes));

            // give the buffer back to the decoder
            in.resetReaderIndex();

            ProtocolType protocol;

            try {
                protocol = ProtocolDetector.detect(protocodBytes);
                logger.info("{} - Protocol type: {}", thisId, protocol);
                invalidBytesCount = 0; // reset if valid
            } catch (IllegalArgumentException e) {
                invalidBytesCount++;

                logger.warn("{} - Invalid protocol ID {}. Resynchronization Attempt {}/{}",
                        thisId, protocodBytes, invalidBytesCount, maxInvalidBytes);

                in.skipBytes(1);

                if (invalidBytesCount >= maxInvalidBytes) {
                    logger.error("{} - Resynchronization failed.", thisId);
                    createErrorTxContext(ctx, in, null);
                    throw new IllegalStateException("Too many invalid bytes.");
                }

                return;
            }

            activeDecoder = decoders.get(protocol);
            logger.info("{} - Protocol Decoder: {}", thisId, activeDecoder);

            if (activeDecoder == null){
                logger.error("{} - No decoder found for protocol {}.", thisId, protocol);
                createErrorTxContext(ctx, in, protocol);
                throw new IllegalStateException("No decoder for protocol: " + protocol + ".");
            }
        }

        logger.info("{} - Delegating to protocol decoder: {}", thisId, activeDecoder);
        ProtocolFrame frame = activeDecoder.decode(in);
        logger.info("{} - Protocol frame: {}", thisId, frame);

        if (frame == null) {
            logger.info("{} - Protocol frame is null.", thisId);
            return; // need more data
        }

        ctx.channel().attr(ChannelAttributes.TX_CONTEXT).set(frame.context());

        out.add(frame);
    }

    private void createErrorTxContext(ChannelHandlerContext ctx, ByteBuf in, ProtocolType protocol) {
        TransactionContext transactionContext;

        if (protocol == null)
            transactionContext = new TransactionContext(in);
        else
            transactionContext = new TransactionContext(in, protocol);

        ctx.channel().attr(ChannelAttributes.TX_CONTEXT).set(transactionContext);
    }
}

