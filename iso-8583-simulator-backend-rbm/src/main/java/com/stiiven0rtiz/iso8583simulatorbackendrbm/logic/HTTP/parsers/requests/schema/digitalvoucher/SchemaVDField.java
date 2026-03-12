package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests.schema.digitalvoucher;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SchemaVDField {
    private String name;
    private int position;
    private int bytes;
    private String type;
    private String format;
    private List<String> possible_values;
    private boolean required;
}