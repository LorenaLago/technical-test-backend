package com.playtomic.tests.wallet.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "WALLET_HISTORIC_ENTITY")
@Getter
public class WalletHistoricEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue
    private String id;

    @Column(name = "WALLET_ID")
    private String walletId;

    @Column(name = "BALANCE")
    private BigDecimal balance;

    @Column(name = "TRANSACTION_DATE")
    private Instant transactionDate;

    @Column(name = "TRANSACTION_ID")
    private String transactionId;

    public WalletHistoricEntity(String walletId, BigDecimal balance, Instant transactionDate, String transactionId) {
        this.walletId = walletId;
        this.balance = balance;
        this.transactionDate = transactionDate;
        this.transactionId = transactionId;
    }

    public WalletHistoricEntity() {

    }
}
