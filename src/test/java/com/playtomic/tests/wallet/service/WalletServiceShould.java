package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.infrastructure.WalletEntity;
import com.playtomic.tests.wallet.infrastructure.WalletHistoricRepository;
import com.playtomic.tests.wallet.infrastructure.WalletRepository;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceShould {

    private WalletService service;

    @Mock
    private WalletRepository repository;

    @Mock
    private WalletHistoricRepository historicRepository;

    @Mock
    private StripeService stripeService;

    @BeforeEach
    public void setUp() {
        service = new WalletService(repository,historicRepository, stripeService);
    }

    @Test
    public void returnAWallet() throws WalletNotFoundException {
        given(this.repository.getWallet("someId")).willReturn(Optional.of(this.getWalletEntity()));

        Wallet wallet = service.getWallet("someId");

        assertNotNull(wallet);
        assertEquals("someId", wallet.getId());
        assertEquals(new BigDecimal(5000), wallet.getCurrentBalance());
    }

    @Test
    public void throwAnErrorWhenCannotFindWallet() {
        given(this.repository.getWallet("someId")).willThrow(new WalletNotFoundException());

        assertThrowsExactly(WalletNotFoundException.class, () -> service.getWallet("someId"));
    }

    @Test
    public void topUpAWallet() throws WalletNotFoundException {
        given(this.repository.getWallet("someId")).willReturn(Optional.of(this.getWalletEntity()));
        given(this.stripeService.charge(any(), any())).willReturn(new Payment("payment_id"));

        service.topUp("someId", "4111111111111111", new BigDecimal(5000));

        InOrder inOrder = Mockito.inOrder(stripeService,repository,historicRepository);
        verify(stripeService).charge("4111111111111111", new BigDecimal(5000));
        verify(repository).save(any());
        verify(historicRepository).save(any());
    }


    @Test
    public void returnAnErrorWhenStripeFailsToTopup() {
        given(this.repository.getWallet("someId")).willReturn(Optional.of(this.getWalletEntity()));
        given(this.stripeService.charge("4111111111111111", new BigDecimal(0))).willThrow( new StripeServiceException());

        assertThrowsExactly(StripeServiceException.class, () -> service.topUp("someId", "4111111111111111", new BigDecimal(0)));
        verify(stripeService).charge("4111111111111111", new BigDecimal(0));

    }

    @Test
    public void doNotStripeWhenThereIsNoWallet() throws WalletNotFoundException {
        given(this.repository.getWallet("someId")).willReturn(Optional.empty());

        assertThrowsExactly(WalletNotFoundException.class, () -> service.topUp("someId", "4111111111111111", new BigDecimal(5000)));

        verifyNoInteractions(stripeService);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(historicRepository);
    }

    @Test
    public void refundAmountWhenUpdateFails() throws WalletNotFoundException {
        given(this.repository.getWallet("someId")).willReturn(Optional.of(this.getWalletEntity()));
        given(this.repository.save(new WalletEntity("someId",new BigDecimal(10000)))).willThrow(HibernateException.class);
        given(this.stripeService.charge(any(), any())).willReturn(new Payment("payment_id"));

        service.topUp("someId", "4111111111111111", new BigDecimal(5000));

        verify(stripeService).charge("4111111111111111", new BigDecimal(5000));
        verifyNoInteractions(historicRepository);
        verify(stripeService).refund("payment_id");
    }

    @Test
    public void refundAmountWhenHistoricFails() throws WalletNotFoundException {
        given(this.repository.getWallet("someId")).willReturn(Optional.of(this.getWalletEntity()));
        given(this.historicRepository.save(any())).willThrow(HibernateException.class);
        given(this.stripeService.charge(any(), any())).willReturn(new Payment("payment_id"));

        service.topUp("someId", "4111111111111111", new BigDecimal(5000));

        verify(stripeService).charge("4111111111111111", new BigDecimal(5000));
        verify(repository).save(new WalletEntity("someId", new BigDecimal(10000)));
        verify(stripeService).refund("payment_id");
        verify(repository).save(new WalletEntity("someId", new BigDecimal(5000)));
    }


    private WalletEntity getWalletEntity() {
        return new WalletEntity("someId", new BigDecimal(5000));
    }

}
