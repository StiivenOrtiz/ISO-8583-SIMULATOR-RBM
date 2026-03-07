package com.stiiven0rtiz.iso8583simulatorbackend.config.txsConfig;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TransactionNamesConfig {
    private Map<String, String> names;
}
