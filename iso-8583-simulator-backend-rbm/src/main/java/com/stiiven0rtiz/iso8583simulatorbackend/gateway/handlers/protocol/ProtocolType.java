package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

import lombok.Getter;

@Getter
public enum ProtocolType {
    ISO8583(0x60),
    UNKNOWN(0x99);

    private final int protocolId;

    ProtocolType(int protocolId) {
        this.protocolId = protocolId;
    }

    public static ProtocolType from(int value) {
        for (ProtocolType p : values())
            if (p.protocolId == value) return p;
        throw new IllegalArgumentException("Unknown protocol: " + value);
    }
}
