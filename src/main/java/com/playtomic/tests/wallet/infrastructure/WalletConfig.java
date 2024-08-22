package com.playtomic.tests.wallet.infrastructure;

import com.playtomic.tests.wallet.api.WalletController;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class WalletConfig {

    @Value(value = "${stripe.simulator.charges-uri}")
    private String chargesUri;

    @Value("${stripe.simulator.refunds-uri}")
    private String refundsUri;

    @Bean
    public StripeService stripeService() {
        return new StripeService(URI.create(chargesUri), URI.create(refundsUri), new RestTemplateBuilder());
    }

    @Bean
    public WalletService walletService(WalletRepository walletRepository, WalletHistoricRepository walletHistoricRepository, StripeService stripeService) {
        return new WalletService(walletRepository, walletHistoricRepository, stripeService);
    }

    @Bean
    public WalletController walletController(@Autowired WalletService walletService) {
        return new WalletController(walletService);
    }

}
