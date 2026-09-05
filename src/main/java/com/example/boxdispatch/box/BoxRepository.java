package com.example.boxdispatch.box;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoxRepository extends JpaRepository<Box, UUID> {

    Optional<Box> findByTxref(String txref);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b
        FROM Box b
        WHERE b.txref = :txref
    """)
    Optional<Box> findByTxrefForUpdate(@Param("txref") String txref);

    List<Box> findAllByStateAndBatteryPercentageGreaterThanEqual(
        BoxState state,
        int batteryPercentage
    );
}
