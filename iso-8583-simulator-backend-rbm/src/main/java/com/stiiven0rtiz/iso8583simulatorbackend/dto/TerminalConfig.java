package com.stiiven0rtiz.iso8583simulatorbackend.dto;

public record TerminalConfig(
        String idDB,
        String terminalID,
        int delay,
        String responseCode
){ }