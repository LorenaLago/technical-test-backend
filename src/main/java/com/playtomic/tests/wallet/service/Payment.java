package com.playtomic.tests.wallet.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;


@Getter
public class Payment {

    @NonNull
    private final String id;

    @JsonCreator
    public Payment(@JsonProperty(value = "id", required = true) @NonNull String id) {
        this.id = id;
    }
}
