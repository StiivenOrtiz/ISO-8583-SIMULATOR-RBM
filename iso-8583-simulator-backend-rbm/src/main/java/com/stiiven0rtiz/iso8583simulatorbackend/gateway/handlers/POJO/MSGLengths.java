package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.POJO;


// imports
import lombok.Getter;

/**
 * Class to hold the lengths of a message field and its content.
 */
@Getter
public class MSGLengths {
    int totalLength;
    int contentLength;

    public MSGLengths(int fieldLength, int contentLength, boolean rounded) {
        this.totalLength = fieldLength + contentLength;
        this.contentLength = (contentLength * 2) - (rounded ? 1 : 0);
    }
}
