package com.stiiven0rtiz.iso8583simulatorbackendrbm.repositories;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TransactionRepository.java
 * <p>
 * This interface extends JpaRepository to provide CRUD operations and custom queries
 * for Transaction entities.
 *
 * @version 1.0
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByTxTimestampBetween(
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("select distinct t.terminal from Transaction t where t.terminal is not null order by t.terminal")
    List<String> findDistinctTerminals();

    @Query("select distinct t.franchise from Transaction t where t.franchise is not null order by t.franchise")
    List<String> findDistinctFranchises();

    @Query("select distinct t.transactionType from Transaction t where t.transactionType is not null order by t.transactionType")
    List<String> findDistinctTransactionTypes();

    @Query("select distinct t.mti from Transaction t where t.mti is not null order by t.mti")
    List<String> findDistinctMTIs();

    @Query("select distinct t.status from Transaction t where t.status is not null order by t.status")
    List<String> findDistinctStatusValues();

    @Query("""
                SELECT COUNT(t)
                FROM Transaction t
                WHERE t.txTimestamp BETWEEN :start AND :end
            """)
    long countByRange(LocalDateTime start, LocalDateTime end);

    @Query("""
                SELECT CAST(t.txTimestamp AS date), COUNT(t)
                FROM Transaction t
                WHERE t.txTimestamp >= :start AND t.txTimestamp < :end
                GROUP BY CAST(t.txTimestamp AS date)
                ORDER BY CAST(t.txTimestamp AS date)
            """)
    List<Object[]> countGroupedByDay(LocalDateTime start, LocalDateTime end);

    @Query("""
                SELECT EXTRACT(HOUR FROM t.txTimestamp), COUNT(t)
                FROM Transaction t
                WHERE t.txTimestamp >= :start AND t.txTimestamp < :end
                GROUP BY EXTRACT(HOUR FROM t.txTimestamp)
                ORDER BY EXTRACT(HOUR FROM t.txTimestamp)
            """)
    List<Object[]> countGroupedByHour(LocalDateTime start, LocalDateTime end);
}