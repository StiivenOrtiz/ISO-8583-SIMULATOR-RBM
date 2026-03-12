package com.stiiven0rtiz.iso8583simulatorbackendrbm.config.txsConfig;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class TerminalsConfig {
    Map<String, String> posTerminals;
}
