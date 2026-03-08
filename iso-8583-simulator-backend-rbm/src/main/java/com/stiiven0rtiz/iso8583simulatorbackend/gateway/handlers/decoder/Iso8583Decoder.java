package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.decoder;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.POJO.MSGLengths;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.DecodedIso8583Metadata;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.SupportsProtocol;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.config.IsoFieldsData;
import com.stiiven0rtiz.iso8583simulatorbackend.utils.IsoUtils;
import io.netty.buffer.ByteBuf;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.stiiven0rtiz.iso8583simulatorbackend.utils.IsoUtils.*;

@SupportsProtocol(ProtocolType.ISO8583)
public class Iso8583Decoder implements ProtocolFrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(Iso8583Decoder.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private enum State {
        TPDU,
        MTI,
        BITMAP,
        DATA_ELEMENTS
    }

    private final IsoFieldsData iso;

    public Iso8583Decoder(IsoFieldsData iso) {
        this.iso = iso;
    }

    @Override
    public ProtocolFrame decode(ByteBuf in) {

        logger.info("{} - Decoding ISO 8583 frame. Available bytes: {}", thisId, in.readableBytes());

        int start = in.readerIndex();

        // ========== TPDU ==========
        if (in.readableBytes() < iso.getTPDU().length())
            return null;

        logger.info("{} - Reading TPDU", thisId);
        in.skipBytes(iso.getTPDU().length());

        // ========== MTI ==========
        if (in.readableBytes() < iso.getMTI().length()) {
            in.readerIndex(start);
            return null;
        }

        byte[] mti = new byte[iso.getMTI().length()];
        in.readBytes(mti);

        logger.info("{} - MTI: {}", thisId, bcdToString(mti));

        // ========== BITMAP ==========
        if (in.readableBytes() < 8) {
            in.readerIndex(start);
            return null;
        }

        long primary = in.readLong();

        Queue<Integer> localFields = new ArrayDeque<>();
        Map<Integer, MSGLengths> localLengths = new TreeMap<>();

        readBitmap(primary, 1, 64, localFields);

        logger.debug("{} - Primary Bitmap fields present: {}", thisId, localFields);

        // ========== DATA ELEMENTS ==========
        if (!decodeDataElementsStateless(in, localFields, localLengths)) {
            in.readerIndex(start);
            return null;
        }

        // ========== FRAME COMPLETO ==========
        int end = in.readerIndex();
        int length = end - start;

        ByteBuf frame = in.retainedSlice(start, length);

        byte[] data = new byte[length];
        frame.getBytes(0, data);

        logger.info("{} - Full ISO8583 message: {}", thisId, IsoUtils.toHexBString(data));

        return new ProtocolFrame(
                ProtocolType.ISO8583,
                new DecodedIso8583Metadata(frame, localLengths),
                frame
        );
    }

    private boolean decodeDataElementsStateless(
            ByteBuf in,
            Queue<Integer> fields,
            Map<Integer, MSGLengths> dataElementsLengths) {

        while (!fields.isEmpty()) {

            int field = fields.peek();
            String fieldStr = getCompleteID(field);
            var def = iso.getDataElementById(fieldStr);

            String lengthType = def.lengthType();
            String fieldType = def.type();

            Object[] lenCalc = getByteLength(fieldType, def.length());
            int fieldLength = (int) lenCalc[0];
            int contentLength = 0;
            boolean rounded = false;

            logger.debug("{} - DecodeDataElementsStateless field: {}, lengthType: {}, fieldType: {}, calculated fieldLength: {}",
                    thisId, fieldStr, lengthType, fieldType, fieldLength);

            if ("LLVAR".equals(lengthType)) fieldLength = 1;
            else if ("LLLVAR".equals(lengthType)) fieldLength = 2;

            int fieldStart = in.readerIndex();

            if (!"F".equals(lengthType)) {

                if (in.readableBytes() < fieldLength) {
                    in.readerIndex(fieldStart);
                    return false;
                }

                byte[] lenBytes = new byte[fieldLength];
                in.readBytes(lenBytes);

                lenCalc = getByteLength(fieldType, bcdToInt(lenBytes));
                contentLength = (int) lenCalc[0];
                rounded = (boolean) lenCalc[1];

                if (in.readableBytes() < contentLength) {
                    in.readerIndex(fieldStart);
                    return false;
                }

                in.skipBytes(contentLength);

            } else {

                if (in.readableBytes() < fieldLength) {
                    in.readerIndex(fieldStart);
                    return false;
                }

                if (fieldStr.equals("P1")) {
                    long secondary = in.getLong(in.readerIndex());
                    readBitmap(secondary, 65, 128, fields);
                }

                if (fieldStr.equals("S65")) {
                    long tertiary = in.getLong(in.readerIndex());
                    readBitmap(tertiary, 129, 192, fields);
                }

                in.skipBytes(fieldLength);
            }

            dataElementsLengths.put(field, new MSGLengths(fieldLength, contentLength, rounded));
            fields.poll();
        }

        return true;
    }

    private void readBitmap(long bitmap, int start, int end, char prefix, Queue<String> fields) {
        // Parse the primary bitmap to determine which fields are present
        for (int i = start; i <= end; i++)
            // For this, we check if the i-th bit is set in the primary bitmap
            if ((bitmap & (1L << (64 - i))) != 0)
                fields.add(prefix + String.valueOf(i)); // Add the field number to the queue if it is present
    }

    private void readBitmap(long bitmap, int start, int end, Queue<Integer> fields) {
        for (int i = start; i <= end; i++)
            if ((bitmap & (1L << (64 - i))) != 0)
                fields.add(i);
    }
}
