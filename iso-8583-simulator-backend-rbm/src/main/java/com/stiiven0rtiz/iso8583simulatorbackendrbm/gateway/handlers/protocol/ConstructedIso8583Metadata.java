package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.message.Iso8583Msg;

public record ConstructedIso8583Metadata(
        Iso8583Msg iso8583Msg
) implements ProtocolMetadata {
}

