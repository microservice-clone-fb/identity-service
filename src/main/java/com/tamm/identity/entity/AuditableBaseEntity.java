package com.tamm.identity.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import com.tamm.identity.utils.AuditListener;

import lombok.Getter;
import lombok.Setter;

// @Deprecated
@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditListener.class)
public abstract class AuditableBaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private Instant createdAt;

    @Column(name = "last_updated_at", columnDefinition = "TIMESTAMP")
    private Instant lastUpdatedAt;

    @Column(name = "created_by", updatable = false, length = 255)
    private String createdBy;

    @Column(name = "last_updated_by", length = 255)
    private String lastUpdatedBy;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean isActive = true;

    @Column(name = "history", columnDefinition = "TEXT")
    private String history;

    // Helper method để thêm history entry
    public void addHistoryEntry(String entry) {
        if (this.history == null) {
            this.history = entry;
        } else {
            this.history = this.history + "\n" + entry;
        }
    }
}
