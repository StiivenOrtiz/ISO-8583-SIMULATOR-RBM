package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

import lombok.Getter;

@Getter
public enum ProtocolType {
    ISO8583,
    HTTP,
    UNKNOWN
}
