package com.clinicsalon.finance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID appointmentId;
    
    @Column(nullable = false)
    private UUID clientId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    private String paymentMethod;
    
    private String paymentGatewayId;
    
    private String invoiceUrl;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String description;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
