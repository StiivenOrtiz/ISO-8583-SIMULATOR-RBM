package com.stiiven0rtiz.iso8583simulatorbackend.models;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> filter(
            String terminal,
            String franchise,
            String transactionType,
            String mti,
            String status,
            String responseCode,
            String authCode,
            String rrn,
            Boolean responseCodeEmpty,
            Boolean authCodeEmpty,
            Boolean rrnEmpty,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            BigDecimal amountFrom,
            BigDecimal amountTo,
            String searchText
    ) {
        return (Root<Transaction> root,
                CriteriaQuery<?> query,
                CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (terminal != null)
                predicates.add(cb.equal(root.get("terminal"), terminal));

            if (franchise != null)
                predicates.add(cb.equal(root.get("franchise"), franchise));

            if (transactionType != null)
                predicates.add(cb.equal(root.get("transactionType"), transactionType));

            if (mti != null)
                predicates.add(cb.equal(root.get("mti"), mti));

            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));

            if (Boolean.TRUE.equals(responseCodeEmpty))
                predicates.add(cb.or(cb.isNull(root.get("responseCode")), cb.equal(cb.trim(root.get("responseCode")), "")));
            else if (responseCode != null && !responseCode.isBlank())
                predicates.add(cb.like(cb.lower(root.get("responseCode")), "%" + responseCode.toLowerCase() + "%"));

            if (Boolean.TRUE.equals(authCodeEmpty))
                predicates.add(cb.or(cb.isNull(root.get("authCode")), cb.equal(cb.trim(root.get("authCode")), "")));
            else if (authCode != null && !authCode.isBlank())
                predicates.add(cb.like(cb.lower(root.get("authCode")), "%" + authCode.toLowerCase() + "%"));

            if (Boolean.TRUE.equals(rrnEmpty))
                predicates.add(cb.or(cb.isNull(root.get("rrn")), cb.equal(cb.trim(root.get("rrn")), "")));
            else if (rrn != null && !rrn.isBlank())
                predicates.add(cb.like(cb.lower(root.get("rrn")), "%" + rrn.toLowerCase() + "%"));

            if (dateFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("txTimestamp"), dateFrom));

            if (dateTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("txTimestamp"), dateTo));

            if (amountFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), amountFrom));

            if (amountTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), amountTo));

            if (searchText != null && !searchText.isBlank()) {
                String like = "%" + searchText.toLowerCase() + "%";

                Join<Transaction, Iso8583Field> isoJoin = root.join("iso8583Fields", JoinType.LEFT);

                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("terminal")), like),
                        cb.like(cb.lower(root.get("mti")), like),
                        cb.like(cb.lower(root.get("franchise")), like),
                        cb.like(cb.lower(root.get("status")), like),
                        cb.like(cb.lower(root.get("responseCode")), like),
                        cb.like(cb.lower(root.get("authCode")), like),
                        cb.like(cb.lower(root.get("rrn")), like),
                        cb.like(cb.lower(root.get("transactionType")), like),
                        cb.like(cb.lower(root.get("bitmapPrimary")), like),
                        cb.like(cb.lower(root.get("bitmapSecondary")), like),
                        cb.like(cb.lower(isoJoin.get("fieldValue").as(String.class)), like),
                        cb.like(cb.lower(root.get("hexRequest").as(String.class)), like),
                        cb.like(cb.lower(root.get("hexResponse").as(String.class)), like)
                );


                predicates.add(searchPredicate);
                assert query != null;
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
