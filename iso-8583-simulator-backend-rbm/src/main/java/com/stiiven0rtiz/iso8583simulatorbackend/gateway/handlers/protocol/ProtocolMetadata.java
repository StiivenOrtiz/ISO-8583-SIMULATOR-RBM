package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

public sealed interface ProtocolMetadata
        permits ConstructedIso8583Metadata, DecodedIso8583Metadata, ResponseMetadata { }
