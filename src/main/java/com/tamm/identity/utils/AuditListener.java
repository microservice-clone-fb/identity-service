package com.tamm.identity.utils;

import java.time.Instant;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import com.tamm.identity.entity.AuditableBaseEntity;
import com.tamm.identity.service.AuditServices;

/**
 * JPA Entity Listener cho audit functionality
 * Không dùng @Component vì sẽ được inject thủ công
 */

// @Deprecated
public class AuditListener {

    private AuditServices auditService;

    public AuditListener() {
        this.auditService = new AuditServices();
    }

    @PrePersist
    public void prePersist(AuditableBaseEntity entity) {
        Instant now = Instant.now();
        String currentUser = auditService.getCurrentUsername();

        // Set audit fields
        entity.setCreatedAt(now);
        entity.setLastUpdatedAt(now);
        entity.setCreatedBy(currentUser);
        entity.setLastUpdatedBy(currentUser);

        // Add history entry
        String historyEntry = auditService.createHistoryEntry(currentUser, "CREATED", now);
        entity.addHistoryEntry(historyEntry);
    }

    @PreUpdate
    public void preUpdate(AuditableBaseEntity entity) {
        Instant now = Instant.now();
        String currentUser = auditService.getCurrentUsername();

        // Update audit fields
        entity.setLastUpdatedAt(now);
        entity.setLastUpdatedBy(currentUser);

        // Add history entry
        String historyEntry = auditService.createHistoryEntry(currentUser, "UPDATED", now);
        entity.addHistoryEntry(historyEntry);
    }
}
