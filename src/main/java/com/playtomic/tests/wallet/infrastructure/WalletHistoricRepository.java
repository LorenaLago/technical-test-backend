package com.playtomic.tests.wallet.infrastructure;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletHistoricRepository extends CrudRepository<WalletHistoricEntity, String> {


}
