package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.decoder;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.DecodedHTTPMetadata;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.SupportsProtocol;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.BytesParser.bytesToHex;

@SupportsProtocol(ProtocolType.HTTP)
public class HTTPDecoder implements ProtocolFrameDecoder {
    String thisId = toString().substring(toString().indexOf("@"));
    private static final Logger logger = LoggerFactory.getLogger(HTTPDecoder.class);

    private int tpduLength;

    public HTTPDecoder(int tpduLength) {
        this.tpduLength = tpduLength;
    }

    @Override
    public ProtocolFrame decode(ByteBuf in) {

        int headerEnd = findHeaderEnd(in);

        if (headerEnd == -1)
            return null;

        int headerLength = headerEnd - in.readerIndex();

        byte[] headerBytes = new byte[headerLength];
        in.getBytes(in.readerIndex(), headerBytes);

        String headers = new String(headerBytes, StandardCharsets.US_ASCII);

        boolean chunked = isChunked(headers);

        int bodyLength;
        int totalLength;
        int contentLength = 0;

        if (chunked) {

            int chunkEnd = findChunkedEnd(in, headerEnd);

            if (chunkEnd == -1)
                return null;

            bodyLength = chunkEnd - headerEnd;
            totalLength = headerLength + bodyLength;

        } else {

            contentLength = extractContentLength(headers);

            bodyLength = contentLength;
            totalLength = headerLength + contentLength;

            if (in.readableBytes() < totalLength)
                return null;
        }

        byte[] fullMessage = new byte[totalLength];
        in.readBytes(fullMessage);

        logger.info("{} - Received HTTP message: {}", thisId, bytesToHex(fullMessage).replace(" ", ""));

        DecodedHTTPMetadata metadata = new DecodedHTTPMetadata(
                tpduLength,
                headerLength - tpduLength,
                bodyLength,
                totalLength,
                chunked,
                contentLength,
                fullMessage
        );

        logger.info("{} - Received HTTP metadata: {}", thisId, metadata);

        return new ProtocolFrame(
                ProtocolType.HTTP,
                metadata,
                fullMessage
        );
    }

    /**
     * Searches for the end of the HTTP headers.
     * HTTP headers terminate with the sequence CRLF CRLF (\r\n\r\n).
     */
    private int findHeaderEnd(ByteBuf buffer) {
        int readerIndex = buffer.readerIndex();
        int writerIndex = buffer.writerIndex();

        for (int i = readerIndex; i < writerIndex - 3; i++)
            if (buffer.getByte(i) == 0x0D && buffer.getByte(i + 1) == 0x0A && buffer.getByte(i + 2) == 0x0D && buffer.getByte(i + 3) == 0x0A)
                return i + 4;

        return -1;
    }

    /**
     * Extracts the Content-Length header value.
     * Returns 0 if the header is not present.
     */
    private int extractContentLength(String headers) {
        for (String line : headers.split("\r\n"))
            if (line.toLowerCase().startsWith("content-length"))
                return Integer.parseInt(line.split(":")[1].trim());

        return 0;
    }

    /**
     * Checks if the request uses chunked transfer encoding.
     */
    private boolean isChunked(String headers) {
        for (String line : headers.split("\r\n"))
            if (line.toLowerCase().startsWith("transfer-encoding") && line.toLowerCase().contains("chunked"))
                return true;

        return false;
    }

    /**
     * Parses the chunked body to determine the end of the message.
     * Each chunk follows the format:
     * <p>
     * <chunk-size in hex>\r\n
     * <chunk-data>\r\n
     * <p>
     * The final chunk has size 0.
     */
    private int findChunkedEnd(ByteBuf buffer, int startIndex) {
        int index = startIndex;

        while (true) {
            // Find end of the line containing the chunk size
            int lineEnd = findCRLF(buffer, index);

            if (lineEnd == -1)
                return -1;

            // Parse chunk size (hexadecimal)
            String sizeHex = buffer.toString(index, lineEnd - index, StandardCharsets.US_ASCII).trim();
            int chunkSize = Integer.parseInt(sizeHex, 16);

            // Move index to the beginning of chunk data
            index = lineEnd + 2;

            // A chunk size of 0 indicates the end of the body
            if (chunkSize == 0) {
                // Ensure the final CRLF exists
                if (buffer.writerIndex() < index + 2)
                    return -1;

                return index + 2;
            }

            // Skip chunk data + trailing CRLF
            index += chunkSize + 2;

            if (buffer.writerIndex() < index)
                return -1;
        }
    }

    /**
     * Finds the next CRLF sequence starting from a given index.
     */
    private int findCRLF(ByteBuf buffer, int index) {
        int writerIndex = buffer.writerIndex();

        for (int i = index; i < writerIndex - 1; i++)
            if (buffer.getByte(i) == 0x0D && buffer.getByte(i + 1) == 0x0A)
                return i;

        return -1;
    }
}