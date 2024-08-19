package com.playtomic.tests.wallet.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "WALLET")
public class WalletEntity {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "CURRENT_BALANCE")
    private BigDecimal currentBalance;


    public WalletEntity(String id, BigDecimal currentBalance) {
        this.id = id;
        this.currentBalance = currentBalance;
    }

    public WalletEntity() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalletEntity that = (WalletEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(currentBalance, that.currentBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, currentBalance);
    }

    @Override
    public String toString() {
        return "WalletEntity{" +
                "id='" + id + '\'' +
                ", currentBalance=" + currentBalance +
                '}';
    }
}
