package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.decoder;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.DecodedHTTPMetadata;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.SupportsProtocol;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.util.BytesParser.bytesToHexNoSpace;

@SupportsProtocol(ProtocolType.HTTP)
public class HTTPDecoder implements ProtocolFrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(HTTPDecoder.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final int tpduLength;

    public HTTPDecoder(int tpduLength) {
        this.tpduLength = tpduLength;
    }

    @Override
    public ProtocolFrame decode(ByteBuf in) {
        int readerIndex = in.readerIndex();
        int writerIndex = in.writerIndex();

        int headerEnd = -1;
        int contentLength = 0;
        boolean chunked = false;

        int lineStart = readerIndex;

        int[] crlf = new int[16];
        int crlfCount = 0;

        // ===== Scan headers once =====
        for (int i = readerIndex; i < writerIndex - 1; i++) {
            if (in.getByte(i) == '\r' && in.getByte(i + 1) == '\n') {
                int pos = i - readerIndex;

                if (crlfCount == crlf.length) {
                    int[] tmp = new int[crlf.length * 2];
                    System.arraycopy(crlf, 0, tmp, 0, crlf.length);
                    crlf = tmp;
                }

                crlf[crlfCount++] = pos;

                int lineLength = i - lineStart;

                if (lineLength == 0) {
                    headerEnd = i + 2;
                    break;
                }

                if (matchHeader(in, lineStart, lineLength, "Content-Length"))
                    contentLength = parseInt(in, lineStart + 15, lineLength - 15);

                if (matchHeader(in, lineStart, lineLength, "Transfer-Encoding"))
                    chunked = containsChunked(in, lineStart, lineLength);

                lineStart = i + 2;
            }
        }

        if (headerEnd == -1)
            return null;

        int headerLength = headerEnd - readerIndex;

        int bodyLength;
        int totalLength;

        if (chunked) {
            int chunkEnd = findChunkedEnd(in, headerEnd);

            if (chunkEnd == -1)
                return null;

            bodyLength = chunkEnd - headerEnd;
            totalLength = headerLength + bodyLength;

        } else {
            bodyLength = contentLength;
            totalLength = headerLength + bodyLength;

            if (in.readableBytes() < totalLength)
                return null;
        }

        // ===== get total message
        byte[] message = new byte[totalLength];
        in.getBytes(in.readerIndex(), message);
        in.skipBytes(totalLength);


        // shrink CRLF array
        int[] finalCRLF = new int[crlfCount];
        System.arraycopy(crlf, 0, finalCRLF, 0, crlfCount);

        logger.info("HTTP message: {}", bytesToHexNoSpace(message).replace(" ", ""));

        for (int j : finalCRLF)
            logger.info("{} - CRLF at position: {}", thisId, j);


        DecodedHTTPMetadata metadata = new DecodedHTTPMetadata(
                tpduLength,
                headerLength - tpduLength,
                bodyLength,
                totalLength,
                chunked,
                contentLength,
                finalCRLF,
                message
        );

        return new ProtocolFrame(
                ProtocolType.HTTP,
                metadata,
                message
        );
    }

    // ===============================
    // Header utils
    // ===============================

    private boolean matchHeader(ByteBuf buf, int start, int length, String header) {
        int headerLen = header.length();

        if (length < headerLen)
            return false;

        for (int i = 0; i < headerLen; i++)
            if (Character.toLowerCase(buf.getByte(start + i)) != Character.toLowerCase(header.charAt(i)))
                return false;

        return true;
    }

    private boolean containsChunked(ByteBuf buf, int start, int length) {
        for (int i = start; i < start + length - 6; i++) {
            if ((buf.getByte(i) == 'c' || buf.getByte(i) == 'C')
                    && (buf.getByte(i + 1) == 'h' || buf.getByte(i + 1) == 'H')
                    && (buf.getByte(i + 2) == 'u' || buf.getByte(i + 2) == 'U')
                    && (buf.getByte(i + 3) == 'n' || buf.getByte(i + 3) == 'N')
                    && (buf.getByte(i + 4) == 'k' || buf.getByte(i + 4) == 'K')
                    && (buf.getByte(i + 5) == 'e' || buf.getByte(i + 5) == 'E')
                    && (buf.getByte(i + 6) == 'd' || buf.getByte(i + 6) == 'D'))
                return true;
        }

        return false;
    }

    private int parseInt(ByteBuf buf, int start, int length) {
        int value = 0;

        for (int i = start; i < start + length; i++) {
            byte b = buf.getByte(i);
            if (b >= '0' && b <= '9')
                value = value * 10 + (b - '0');
        }

        return value;
    }

    // ===============================
    // Chunked parsing
    // ===============================

    private int findChunkedEnd(ByteBuf buffer, int startIndex) {
        int index = startIndex;

        while (true) {
            int lineEnd = findCRLF(buffer, index);

            if (lineEnd == -1)
                return -1;

            int chunkSize = parseHex(buffer, index, lineEnd - index);

            index = lineEnd + 2;

            if (chunkSize == 0) {
                if (buffer.writerIndex() < index + 2)
                    return -1;
                return index + 2;
            }

            index += chunkSize + 2;

            if (buffer.writerIndex() < index)
                return -1;
        }
    }

    private int parseHex(ByteBuf buf, int start, int length) {
        int value = 0;

        for (int i = start; i < start + length; i++) {
            byte b = buf.getByte(i);

            if (b >= '0' && b <= '9')
                value = (value << 4) + (b - '0');
            else if (b >= 'a' && b <= 'f')
                value = (value << 4) + (b - 'a' + 10);
            else if (b >= 'A' && b <= 'F')
                value = (value << 4) + (b - 'A' + 10);
        }

        return value;
    }

    private int findCRLF(ByteBuf buffer, int index) {
        int writerIndex = buffer.writerIndex();

        for (int i = index; i < writerIndex - 1; i++)
            if (buffer.getByte(i) == '\r' && buffer.getByte(i + 1) == '\n')
                return i;

        return -1;
    }
}