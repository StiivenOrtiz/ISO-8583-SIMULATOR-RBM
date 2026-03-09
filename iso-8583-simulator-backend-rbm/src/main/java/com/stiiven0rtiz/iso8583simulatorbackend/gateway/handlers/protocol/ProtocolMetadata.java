package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

public sealed interface ProtocolMetadata
        permits ConstructedHTTPMetadata, ConstructedIso8583Metadata, DecodedHTTPMetadata, DecodedIso8583Metadata, ResponseMetadata { }
