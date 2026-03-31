package com.orderanddelivery.outbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
            select e.id
            from OutboxEvent e
            where e.status = :status
              and e.nextAttemptAt <= :now
            order by e.id asc
            """)
    List<Long> findIdsReadyForDispatch(
            @Param("status") OutboxEventStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEvent e where e.id = :id")
    Optional<OutboxEvent> findByIdForUpdate(@Param("id") Long id);
}