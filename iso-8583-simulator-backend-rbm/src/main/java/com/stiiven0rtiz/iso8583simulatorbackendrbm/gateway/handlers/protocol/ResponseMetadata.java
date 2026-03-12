package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol;

import lombok.Getter;
import lombok.Setter;

@Getter
public final class ResponseMetadata implements ProtocolMetadata {

    private final ResponseMSG responseMSG;
    private final String rawData;
    private final String TerminalId;
    @Setter
    private int artificialDelay;

    public ResponseMetadata(ResponseMSG responseMSG, String rawData, String terminalId, int artificialDelay) {
        this.responseMSG = responseMSG;
        this.rawData = rawData;
        this.TerminalId = terminalId;
        this.artificialDelay = artificialDelay;
    }

    public ResponseMetadata(ResponseMSG responseMSG, String rawData, String terminalId) {
        this.responseMSG = responseMSG;
        this.rawData = rawData;
        this.TerminalId = terminalId;
    }

}
