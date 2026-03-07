package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.constructor;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.POJO.MSGLengths;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.*;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.config.IsoFieldsData;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.message.Iso8583Msg;
import com.stiiven0rtiz.iso8583simulatorbackend.utils.IsoUtils;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import static com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.HexParser.toHexNoSpace;
import static com.stiiven0rtiz.iso8583simulatorbackend.utils.IsoUtils.*;

@SupportsProtocol(ProtocolType.ISO8583)
public class Iso8583FrameConstructor implements ProtocolFrameConstructor {

    // Logger instance
    private static final Logger logger = LoggerFactory.getLogger(Iso8583FrameConstructor.class);
    String thisId = toString().substring(toString().indexOf("@"));

    // ISO field definitions
    private final IsoFieldsData iso;

    public Iso8583FrameConstructor(IsoFieldsData isoFieldsData) {
        this.iso = isoFieldsData;
    }

    @Override
    public ProtocolFrame constructMSG(ProtocolFrame input) throws Exception {
        DecodedIso8583Metadata iso8583Metadata;

        try {
            iso8583Metadata = (DecodedIso8583Metadata) input.metadata();
        } catch (Exception e) {
            logger.error("{} - Fatal error retrieving ISO8583 metadata: {}", thisId, e.getMessage());
            throw new Exception("ISO8583 Metadata retrieval error", e);
        }

        // do a copy of the message to avoid modifying the original buffer
        ByteBuf msgCopy = iso8583Metadata.message();

        // create a new Iso8583Msg to hold the constructed message
        Iso8583Msg isoMsg = new Iso8583Msg();
        int bytesRead = 0;

        isoMsg.setRawData(toHexNoSpace(msgCopy));
        logger.info("{} - Received message: {}", thisId, isoMsg.getRawData());

        isoMsg.setTPDU(toHex(msgCopy, bytesRead, iso.getTPDU().length()));
        logger.info("{} - Received TPDU: {}", thisId, isoMsg.getTPDU().getValue());
        bytesRead += iso.getTPDU().length();

        isoMsg.setMTI(toHex(msgCopy, iso.getTPDU().length(), iso.getMTI().length()));
        logger.info("{} - Received MTI: {}", thisId, isoMsg.getMTI().getValue());
        bytesRead += iso.getMTI().length();

        isoMsg.setBitmap(toHex(msgCopy, bytesRead, iso.getBITMAP().length()));
        logger.info("{} - Received Bitmap: {}", thisId, isoMsg.getBitmap().getValue());
        bytesRead += iso.getBITMAP().length();

        Map<Integer, MSGLengths> dataElementsLengths =
                new TreeMap<>(iso8583Metadata.dataElementsLengths());
        dataElementsLengths.putAll(iso8583Metadata.dataElementsLengths());

        if (dataElementsLengths.isEmpty())
            logger.warn("{} - No data elements to process.", thisId);

        for (var entry : dataElementsLengths.entrySet()) {
            int fieldId = entry.getKey(); // Get the field ID
            String fieldIdStr = getCompleteID(fieldId);
            MSGLengths lengths = entry.getValue(); // Get the lengths object

            var dataElement = iso.getDataElementById(fieldIdStr); // Retrieve data element definition
            String fieldLengthType = dataElement.lengthType(); // Get length type
            String fieldType = dataElement.type(); // Get field type

            int totalLength = lengths.getTotalLength(); // Get total length
            int contentLength = lengths.getContentLength(); // Get content length

            // Determine the number of bytes used for the length header
            int lengthHeaderBytes = switch (fieldLengthType) {
                case "LLVAR" -> 1;
                case "LLLVAR" -> 2;
                case "F" -> 0;
                default -> throw new IllegalArgumentException(
                        "Unsupported length type: " + fieldLengthType
                );
            };

            // For fixed length fields, set content length to the defined length
            if ("F".equals(fieldLengthType))
                contentLength = dataElement.length();

            // Calculate the length of the field value
            int valueLength = totalLength - lengthHeaderBytes;
            byte[] fieldValueBytes = new byte[valueLength];

            // Extract the field value bytes from the message
            msgCopy.getBytes(msgCopy.readerIndex() + bytesRead + lengthHeaderBytes, fieldValueBytes);

            String fieldValueHex = toHexBString(fieldValueBytes); // Convert field value bytes to hex string

            logger.debug("{} - Field {}: {} - {} , FIELD {}", thisId, fieldId, valueLength, fieldValueHex, fieldType);

            // Post-process the field value based on its type
            String postFieldValue = IsoUtils.getValueStringByType(fieldType, fieldIdStr, fieldValueBytes, contentLength, iso);

            logger.info("{} - Post-processed Field [{}]: {}", thisId, fieldId, postFieldValue);

            // Set the processed field value in the Iso8583Msg
            isoMsg.setDataElement(fieldIdStr, postFieldValue);
            bytesRead += totalLength;
        }

        // release the copied buffer
        msgCopy.release();

        ConstructedIso8583Metadata constructedMetadata = new ConstructedIso8583Metadata(isoMsg, LocalDateTime.now());
        input.context().setConstructedMessage();

        return new ProtocolFrame(ProtocolType.ISO8583, constructedMetadata, input.context());
    }

}
