package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util;

public class HttpProtocolDetector {
    private static final byte[] HTTP_METHODS = {
            'G', 'E', // GET
            'P', 'O', // POST
            'P', 'U', // PUT
            'D', 'E', // DELETE
            'H', 'T', // HTTP (for HTTP/1.0 and HTTP/1.1)
    };

    /**
     * Detects if the incoming data likely corresponds to HTTP
     * by checking the first two ASCII characters of the request.
     */
    public static boolean isHttpProtocol(byte[] data) {
        if (data == null || data.length < 2)
            return false;

        byte b0 = data[0];
        byte b1 = data[1];

        for (int i = 0; i < HTTP_METHODS.length; i += 2)
            if (b0 == HTTP_METHODS[i] && b1 == HTTP_METHODS[i + 1])
                return true;

        return false;
    }
}
