package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.POJO;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.message.Iso8583Msg;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * ConstructedMessage.java
 *
 * This class represents a constructed ISO 8583 message along with its construction date.
 *
 * @version 1.0
 */
@Deprecated
@Getter @Setter
public class ConstructedMessage {
    private Iso8583Msg iso8583Msg;
    private LocalDateTime constructionDate;

    public ConstructedMessage(Iso8583Msg iso8583Msg) {
        this.iso8583Msg = iso8583Msg;
        this.constructionDate = LocalDateTime.now();
    }
}
