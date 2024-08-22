package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.infrastructure.WalletEntity;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;


@Getter
public class Wallet {

    @NonNull
    private String id;

    private BigDecimal currentBalance;

    public Wallet() {
    }

    public Wallet(@NonNull String id, BigDecimal currentBalance) {
        this.id = id;
        this.currentBalance = currentBalance;
    }

    public Wallet from(WalletEntity entity) {
        return new Wallet(entity.getId(), entity.getCurrentBalance());
    }
}
