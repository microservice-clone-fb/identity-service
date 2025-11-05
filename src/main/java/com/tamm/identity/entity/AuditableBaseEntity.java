package com.tamm.identity.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Lob;
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Lob
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
