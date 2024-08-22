package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.service.StripeServiceException;
import com.playtomic.tests.wallet.service.Wallet;
import com.playtomic.tests.wallet.service.WalletNotFoundException;
import com.playtomic.tests.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class WalletController {
    private final Logger log = LoggerFactory.getLogger(WalletController.class);

    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @RequestMapping("/")
    void log() {
        log.info("Logging from /");
    }


    @GetMapping(value = "/wallet/{id}")
    public ResponseEntity<Wallet> getWallet(@PathVariable("id") String id) {
        try {
            Wallet wallet = walletService.getWallet(id);
            return new ResponseEntity<>(wallet, HttpStatus.OK);
        } catch (WalletNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/wallet/{id}/topUp")
    public ResponseEntity<Void> topUp(@PathVariable("id") String id,
                                      @RequestParam("creditCardNumber") String creditCardNumber,
                                      @RequestParam("amount") BigDecimal amount) {
        try {
            walletService.topUp(id, creditCardNumber, amount);
            return ResponseEntity.ok().build();
        } catch (WalletNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (StripeServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
