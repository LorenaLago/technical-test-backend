package com.playtomic.tests.wallet.infrastructure;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface WalletRepository extends CrudRepository<WalletEntity, String> {

    @Transactional
    Optional<WalletEntity> findById(String id);

}
