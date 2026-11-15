package com.wss.csrf.model;

import jakarta.persistence.*;
import java.time.Instant;

/** A record of a sensitive action. Crucially, a CSRF-forged change is logged
 *  against the VICTIM (the admin whose session was abused) — so the audit trail
 *  wrongly blames alice for promoting the attacker. */
@Entity
@Table(name = "audit_log")
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected AuditEntry() { }

    public AuditEntry(String actor, String action) {
        this.actor = actor;
        this.action = action;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public Instant getCreatedAt() { return createdAt; }
}
