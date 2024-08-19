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
public class WalletController {
    private final Logger log = LoggerFactory.getLogger(WalletController.class);

    private final WalletService service;

    @Autowired
    public WalletController(WalletService service) {
        this.service = service;
    }

    @RequestMapping("/")
    void log() {
        log.info("Logging from /");
    }


    @GetMapping(value = "/wallet/{id}")
    public ResponseEntity<Wallet> getWallet(@PathVariable("id") String id) {
        try {
            Wallet wallet = service.getWallet(id);
            return new ResponseEntity<>(wallet, HttpStatus.OK);
        } catch (WalletNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/wallet/{id}/topUp")
    public ResponseEntity topUp(@PathVariable("id") String id, String creditCardNumber, BigDecimal amount) {
        try {
            service.topUp(id, creditCardNumber, amount);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (WalletNotFoundException | StripeServiceException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
