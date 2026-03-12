package com.stiiven0rtiz.iso8583simulatorbackendrbm.mapper;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.dto.TransactionDto;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Iso8583Field;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.IsoMessageType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public static TransactionDto toDto(Transaction entity) {
        return new TransactionDto(
                String.valueOf(entity.getId()),
                entity.getUuid(),
                getTxTimestamp(entity.getTxTimestamp(), entity.getReceivedAt()),
                entity.getTerminal(),
                entity.getAmount(),
                entity.getFranchise(),
                getFranchiseLogo(entity.getFranchise()),
                entity.getTransactionType(),
                entity.getMti(),
                entity.getProtocol(),
                entity.getStatus().name(),
                entity.getResponseCode(),
                entity.getAuthCode(),
                entity.getRrn(),
                entity.getBitmapPrimary(),
                entity.getBitmapSecondary(),
                toDTOIso8583Fields(filterIsoFieldsByType(entity.getIso8583Fields(), IsoMessageType.REQUEST)),
                toDTOIso8583Fields(filterIsoFieldsByType(entity.getIso8583Fields(), IsoMessageType.RESPONSE)),
                entity.getHexRequest(),
                entity.getHexResponse()
        );
    }

    private static List<Iso8583Field> filterIsoFieldsByType(List<Iso8583Field> fields, IsoMessageType type) {
        return fields.stream()
                .filter(field -> field.getMessageType() == type)
                .collect(Collectors.toList());
    }

    private static Map<String, String> toDTOIso8583Fields(List<Iso8583Field> iso8583Fields) {
        Map<String, String> dataElements = new HashMap<>();
        for (Iso8583Field field : iso8583Fields) {
            assert false;
            dataElements.put(field.getFieldId(), field.getFieldValue());
        }
        return dataElements;
    }

    private static String getFranchiseLogo(String franchise) {
        if (franchise == null)
            return "unknown";

        return franchise.toLowerCase().replaceAll(" ", "");
    }

    private static String getTxTimestamp(LocalDateTime tximestamp, LocalDateTime txReceiptDate) {
        return Objects.requireNonNullElseGet(tximestamp, () -> Objects.requireNonNullElseGet(txReceiptDate, LocalDateTime::now)).toString();
    }
}
