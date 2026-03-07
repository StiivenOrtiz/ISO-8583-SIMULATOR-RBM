package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.decoder.deprecated;


// imports
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.POJO.Iso8583FramedMessage;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.POJO.MSGLengths;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.config.IsoFieldsData;

import com.stiiven0rtiz.iso8583simulatorbackend.services.TransactionService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Queue;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedList;

import static com.stiiven0rtiz.iso8583simulatorbackend.utils.IsoUtils.*;

/**
 * Iso8583FrameDecoder.java
 *
 * This class implements a custom frame decoder for ISO 8583 messages.
 * It extends ByteToMessageDecoder to decode incoming ByteBufs into ISO 8583 messages.
 *
 * @version 1.2
 */
@Deprecated
public class Iso8583FrameDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(Iso8583FrameDecoder.class);
    String thisId = toString().substring(toString().indexOf("@"));

    // States for the decoder for indicating what part of the message we are currently processing
    private static final int STATE_WAITING_TPDU_MTI_BITMAP = 0;
    private static final int STATE_WAITING_DATA_ELEMENTS = 1;

    // Current state of the decoder
    private int currentState = STATE_WAITING_TPDU_MTI_BITMAP;

    // Variables to track the state of the message being decoded
    private int bytesToSkip = -1;
    private int messageStartIndex = -1;

    // Counter for errors encountered during decoding
    private int errorCount = 0;
    private final int maxErrors;
    String discardedMessage = "";
    boolean repeatedErrorLogged = false;

    // Reference to the ISO fields data configuration
    private final IsoFieldsData iso;
    private final TransactionService transactionService;
    private final Queue<String> fields = new LinkedList<>();
    // Map to hold the lengths of data elements as they are parsed
    private final Map<String, MSGLengths> dataElementsLengths = new TreeMap<>(
            Comparator.comparingInt(s -> Integer.parseInt(s.substring(1))));

    /**
     * Constructor for the ISO 8583 custom frame decoder.
     *
     * @param iso The ISO fields data configuration that defines the structure of ISO 8583 messages.
     */
    public Iso8583FrameDecoder(int maxErrors, IsoFieldsData iso, TransactionService transactionService) {
        this.maxErrors = maxErrors;
        this.iso = iso;
        this.transactionService = transactionService;
    }

    /**
     * Called when a new connection is established.
     * @param ctx The ChannelHandlerContext for this handler.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("{} - New connection established from: {}.", thisId, ctx.channel().remoteAddress());
        ctx.fireChannelActive();
    }

    /**
     * Called when the channel becomes inactive, indicating that the connection has been lost.
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
        ctx.close();
    }

    /**
     * Decodes the incoming ByteBuf into a list of objects, processing ISO 8583 messages.
     *
     * @param ctx The ChannelHandlerContext for this handler.
     * @param in  The ByteBuf containing the incoming data.
     * @param out The List to which decoded messages will be added.
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // mark the current reader index to reset later if needed
        in.markReaderIndex();

        // If is the start of a new message, saveConstruction its index
        if (messageStartIndex == -1) {
            messageStartIndex = in.readerIndex();
            logger.debug("{} - Starting framing a new message ISO 8583 at index: {}", thisId, messageStartIndex);
        }

        logger.debug("{} - Framing ISO 8583 message at index {}: {}", thisId, in.readerIndex(), toHex(in, messageStartIndex, in.readableBytes()));

        try {
            if (currentState == STATE_WAITING_TPDU_MTI_BITMAP) {
                bytesToSkip = iso.getTPDU().length() + iso.getMTI().length() + iso.getBITMAP().length();

                if (in.readableBytes() < (bytesToSkip)) {
                    logger.debug("{} - Not enough bytes for TPDU, MTI and primary bitmap. Waiting for more...", thisId);
                    in.resetReaderIndex();
                    return;
                } else
                    logger.debug("{} - Sufficient bytes for TPDU, MTI and primary bitmap. Proceeding with parsing...", thisId);

                // Read MTI and validate it
                bytesToSkip -= iso.getBITMAP().length() + iso.getMTI().length();
                ByteBuf tempBuffer = in.slice(in.readerIndex() + bytesToSkip, in.readableBytes() - bytesToSkip);

                if (!isValidMti(new byte[] { tempBuffer.getByte(0), tempBuffer.getByte(1) })){
                    logger.error("{} - Invalid MTI detected.", thisId);
                    throw new IllegalArgumentException("Invalid MTI.");
                }

                bytesToSkip += iso.getMTI().length();;
                tempBuffer = in.slice(in.readerIndex() + bytesToSkip, in.readableBytes() - bytesToSkip);

                ReadBitMap(tempBuffer.readLong(), 1, 64, 'P');

                logger.debug("{} - Primary bitmap has been parsed.", thisId);
                bytesToSkip += iso.getBITMAP().length();
                currentState = STATE_WAITING_DATA_ELEMENTS; // Update state to waiting for data elements
            }

            if (currentState == STATE_WAITING_DATA_ELEMENTS) { // We are now waiting for the data elements
                if (in.readableBytes() < bytesToSkip) { // Check if we have enough bytes for the data elements
                    logger.debug("{} - Not enough bytes for data elements. Waiting for more...", thisId);
                    in.resetReaderIndex();
                    return;
                }

                logger.debug("Bytes skipped: {}, readableBytes: {}, totalBytes: {}", bytesToSkip, in.readableBytes(), in.readableBytes() + bytesToSkip);

                while (!fields.isEmpty()) { // While there are fields to process
                    String field = fields.peek(); // Peek the next field to process
                    String fieldLengthType = iso.getDataElementById(field).lengthType();
                    String fieldType = iso.getDataElementById(field).type();
                    // Calculate the length of the field based on its type and length
                    Object [] calculatedLength = getByteLength(fieldType, iso.getDataElementById(field).length());
                    int fieldLength = (int) calculatedLength[0];
                    int contentLength = 0;
                    boolean rounded = false;

                    if ("LLVAR".equals(fieldLengthType)) fieldLength = 1;
                    else if ("LLLVAR".equals(fieldLengthType)) fieldLength = 2;

                    if (hasEnoughBytesField(in, (bytesToSkip + fieldLength), field)) return;

                    ByteBuf tempBuffer = in.slice(in.readerIndex() + bytesToSkip, in.readableBytes() - bytesToSkip);

                    if (field.equals("P1")) // Special case for secondary bitmap
                        ReadBitMap(tempBuffer.readLong(), 65, 128, 'S');

                    if (!fieldLengthType.equals("F")) { // If the field is not fixed length
                        byte[] fieldBytes = new byte[fieldLength];
                        tempBuffer.readBytes(fieldBytes);
                        calculatedLength = getByteLength(fieldType, bcdToInt(fieldBytes));
                        contentLength = (int) calculatedLength[0];
                        rounded = (boolean) calculatedLength[1];

                        if (hasEnoughBytesField(in, (bytesToSkip + fieldLength + contentLength), field)) return;

                        bytesToSkip += contentLength; // Increment the bytes to skip by the content length
                    }

                    logger.debug("{} - field {} [{}] has been parsed with length: {}.",
                            thisId, field, iso.getDataElementById(field).name(), fieldLength);

                    dataElementsLengths.put(field, new MSGLengths(fieldLength, contentLength, rounded));
                    fields.poll(); // Remove the processed field from the queue
                    bytesToSkip += fieldLength; // Increment the bytes to skip by the length of the field
                }

                ByteBuf frame = in.readBytes(bytesToSkip);
                byte[] data = new byte[frame.readableBytes()];
                frame.readBytes(data);
                frame.release();

                out.add(new Iso8583FramedMessage(data, dataElementsLengths)); // Send the complete message to the next handler
                logger.info("{} - ISO 8583 message has been framed with {} bytes.", thisId, bytesToSkip);

                if (repeatedErrorLogged) {
                    logger.warn("{} - Resuming normal operation after previous ({}) errors.", thisId, errorCount);
                    repeatedErrorLogged = false;
                    errorCount = 0;
                    logger.warn("{} - Previously discarded bytes (HEX): {}", thisId, discardedMessage.replace(" ", ""));
                    discardedMessage = "";
                }

                ResetFrameDecoder();
            }
        } catch (Exception e) {
            logger.error("{} - Error #{} during ISO 8583 frame decoding. Discarding message. Error; {}",
                    thisId, errorCount + 1, e.getMessage());
            // in.skipBytes(in.readableBytes()); // Discard the rest of the message
            // skip one byte to avoid infinite loop

            // saveConstruction discarded skipped byte
            // Read (and consume) the first discarded byte
            byte discarded = in.readByte();

            // Save the rest of the message in hex
            discardedMessage = discardedMessage + String.format("%02X", discarded);
            repeatedErrorLogged = true;

            errorCount++;
            ResetFrameDecoder();

            if (errorCount >= maxErrors) {
                logger.error("{} - Too many errors ({}) in decoding. Closing channel.", thisId, errorCount);

                // show (all) discarded message on "in"
                discardedMessage = discardedMessage + toHex(in, in.readerIndex(), in.readableBytes());
                discardedMessage = discardedMessage.replace(" ", "");
                logger.error("{} - Discarded message (HEX): {}", thisId, discardedMessage);
                ctx.executor().submit(() -> { transactionService.saveError(discardedMessage, ProtocolType.ISO8583); });

                // Delete "in" content
                in.skipBytes(in.readableBytes());
                ResetFrameDecoder();

                ctx.close(); // Close the channel after too many errors
            }
        }
    }

    /**
     * Resets the frame decoder state variables to prepare for the next message.
     */
    private void ResetFrameDecoder() {
        bytesToSkip = 0;
        messageStartIndex = -1;
        dataElementsLengths.clear();
        fields.clear();
        currentState = STATE_WAITING_TPDU_MTI_BITMAP;
    }

    /**
     * Reads the bitmap and adds the fields to the queue based on the bitmap.
     *
     * @param bitmap The bitmap to read.
     * @param start  The starting field number (1-based).
     * @param end    The ending field number (1-based).
     * @param prefix The prefix to add to the field names (e.g., 'P' for primary, 'S' for secondary).
     */
    private void ReadBitMap(long bitmap, int start, int end, char prefix){
        // Parse the primary bitmap to determine which fields are present
        for (int i = start; i <= end; i++)
            // For this, we check if the i-th bit is set in the primary bitmap
            if ((bitmap & (1L << (64 - i))) != 0)
                fields.add(prefix + String.valueOf(i)); // Add the field number to the queue if it is present
    }

    /**
     * Checks if there are enough bytes in the ByteBuf for the specified field.
     *
     * @param in          The ByteBuf containing the incoming data.
     * @param bytesNeeded The number of bytes needed for the field.
     * @param field       The field name to check.
     * @return true if there are not enough bytes, false otherwise.
     */
    private boolean hasEnoughBytesField(ByteBuf in, int bytesNeeded, String field) {
        logger.debug("{} - readableBytes: {}, bytesNeeded: {}, field: {} [{}]",
                thisId, in.readableBytes(), bytesNeeded, field, iso.getDataElementById(field).name());
        if (in.readableBytes() < bytesNeeded) {
            logger.debug("{} - Not enough bytes for field {} [{}]. Waiting for more...", thisId, field,
                    iso.getDataElementById(field).name());
            in.resetReaderIndex();
            return true;
        }
        return false;
    }

    /**
     * Validates the MTI (Message Type Indicator) from the BCD byte array.
     *
     * @param bcdBytes The BCD byte array containing the MTI.
     * @return true if the MTI is valid, false otherwise.
     */
    private boolean isValidMti(byte[] bcdBytes) {
        String mti = bcdToString(bcdBytes);

        logger.debug("{} - isValidMti: {}", thisId, mti);

        if (mti.length() != 4 || !mti.matches("\\d{4}"))
            return false;

        char version = mti.charAt(0);  // Pos 1
        char messageClass = mti.charAt(1); // Pos 2
        char function = mti.charAt(2); // Pos 3

        boolean versionValid = "0129".indexOf(version) != -1;
        boolean classValid = "123456789".indexOf(messageClass) != -1;
        boolean functionValid = "01234".indexOf(function) != -1;

        return versionValid && classValid && functionValid;
    }
}