package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.infrastructure.WalletEntity;
import com.playtomic.tests.wallet.infrastructure.WalletHistoricEntity;
import com.playtomic.tests.wallet.infrastructure.WalletHistoricRepository;
import com.playtomic.tests.wallet.infrastructure.WalletRepository;
import org.hibernate.HibernateException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
public class WalletService {

    private final WalletRepository repository;
    private final WalletHistoricRepository historicRepository;
    private final StripeService stripeService;

    public WalletService(WalletRepository walletRepository, WalletHistoricRepository historicRepository, StripeService stripeService) {
        this.repository = walletRepository;
        this.stripeService = stripeService;
        this.historicRepository = historicRepository;
    }

    public Wallet getWallet(String someId) throws WalletNotFoundException {
        Wallet wallet = new Wallet();
        Optional<WalletEntity> walletEntity = repository.getWallet(someId);
        if (walletEntity.isPresent()) {
            return wallet.from(walletEntity.get());
        }
        throw new WalletNotFoundException();
    }


    public void topUp(String id, String creditCardNumber, BigDecimal amount) throws WalletNotFoundException, StripeServiceException {
        Payment payment = null;
        Wallet wallet = null;
        try {
            wallet = getWallet(id);
            payment = stripeService.charge(creditCardNumber, amount);

            BigDecimal updatedBalance = wallet.getCurrentBalance().add(amount);
            repository.save(new WalletEntity(id, updatedBalance));
            historicRepository.save(new WalletHistoricEntity(id, updatedBalance, Instant.now(), payment.getId()));

        } catch (HibernateException e) {
            if (payment != null) stripeService.refund(payment.getId());
            if (wallet != null) repository.save(new WalletEntity(id, wallet.getCurrentBalance()));
        }
    }
}
