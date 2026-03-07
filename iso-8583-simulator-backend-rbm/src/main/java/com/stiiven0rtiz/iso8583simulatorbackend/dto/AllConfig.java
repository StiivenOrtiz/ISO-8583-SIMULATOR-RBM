package com.stiiven0rtiz.iso8583simulatorbackend.dto;

import java.util.List;

public record AllConfig(
        boolean globalDelay,
        int globalDelayValue,
        boolean globalResponseCode,
        String globalResponseCodeValue,
        List<TerminalConfig> terminals
) {}
