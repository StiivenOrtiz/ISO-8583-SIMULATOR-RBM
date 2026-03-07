package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

import com.stiiven0rtiz.iso8583simulatorbackend.iso.message.Iso8583Msg;

import java.time.LocalDateTime;

public record ConstructedIso8583Metadata(
        Iso8583Msg iso8583Msg,
        LocalDateTime constructionDate
) implements ProtocolMetadata {
}

