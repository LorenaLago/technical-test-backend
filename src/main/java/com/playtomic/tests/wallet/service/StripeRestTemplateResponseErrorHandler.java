package com.playtomic.tests.wallet.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Component
public class StripeRestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is5xxServerError() || response.getStatusCode().is4xxClientError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
            throw new StripeAmountTooSmallException();
        }else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            throw new StripeServiceException();
        }
    }
}
