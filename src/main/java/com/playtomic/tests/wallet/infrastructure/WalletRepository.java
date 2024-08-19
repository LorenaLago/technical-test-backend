package com.playtomic.tests.wallet.infrastructure;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface WalletRepository extends CrudRepository<WalletEntity, String> {

    @Transactional
    Optional<WalletEntity> getWallet(@Param("id") String id);

}
