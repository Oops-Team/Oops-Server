package com.oops.server.repository;

import com.oops.server.entity.CancelReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancelReasonRepository extends JpaRepository<CancelReason, Long> {
}
