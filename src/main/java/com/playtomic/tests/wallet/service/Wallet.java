package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.infrastructure.WalletEntity;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class Wallet {

    @NonNull
    private String id;

    private BigDecimal currentBalance;

    public @NonNull String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public Wallet from(WalletEntity entity){
        this.id = entity.getId();
        this.currentBalance = entity.getCurrentBalance();
        return this;
    }
}
