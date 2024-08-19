package com.playtomic.tests.wallet;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.playtomic.tests.wallet.api.WalletController;
import com.playtomic.tests.wallet.infrastructure.WalletEntity;
import com.playtomic.tests.wallet.infrastructure.WalletHistoricRepository;
import com.playtomic.tests.wallet.infrastructure.WalletRepository;
import com.playtomic.tests.wallet.service.WalletNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@SpringBootTest(classes = {
        WalletController.class})
@ActiveProfiles(profiles = "test")
public class WalletApplicationIT {

    @Autowired
    private WalletController walletController;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private WalletRepository walletRepository;

    public static WireMockServer wireMockServer = new WireMockServer(7777);


    @BeforeEach
    public void initialize() {
        MockitoAnnotations.initMocks(this);
        reset(this.walletRepository);
        wireMockServer.start();
    }

    @BeforeAll
    public static void start() {
        wireMockServer.start();
    }


    @AfterEach
    public void stop() {
        wireMockServer.stop();
    }

    @Test
    public void emptyTest() {
    }


    @Test
    public void returnAWalletForAGivenId() {
        given(this.walletRepository.getWallet("someId")).willReturn(Optional.of(this.getWalletEntity()));
        given().standaloneSetup(this.walletController)
                .when()
                .get("/api/getWallet/someId")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void returnAnErrorForANotFoundWallet() {
        given(this.walletRepository.getWallet("someId")).willThrow(new WalletNotFoundException());
        given().standaloneSetup(this.walletController)
                .when()
                .get("/api/getWallet/someId")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void topupWallet() throws URISyntaxException, IOException {
        given(this.walletRepository.getWallet("someId")).willReturn(Optional.of(this.getWalletEntity()));

        wireMockServer.stubFor(
                post(urlEqualTo("http://localhost:9999"))
                        .withRequestBody(equalTo(getJsonFromFile("stripeRequest.json")))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        )
        );

        given().standaloneSetup(this.walletController)
                .param("creditCardNumber", "4111111111111111")
                .param("amount", new BigDecimal(5000))
                .when()
                .post("/api/getWallet/someId/topUp")
                .then()
                .statusCode(HttpStatus.OK.value());

    }

    @Test
    public void returnAnErrorWhenTopupWalletFails() throws URISyntaxException, IOException {

        wireMockServer.stubFor(
                post(urlEqualTo("http://localhost:9999"))
                        .withRequestBody(equalTo(getJsonFromFile("stripeRequest.json")))
                        .willReturn(
                                aResponse()
                                        .withStatus(404)
                                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        )
        );

        given(this.walletRepository.getWallet("someId")).willReturn(Optional.of(this.getWalletEntity()));
        given().standaloneSetup(this.walletController)
                .param("creditCardNumber", "411111111111")
                .param("amount", new BigDecimal(0))
                .when()
                .post("/api/getWallet/someId/topUp")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }


    private WalletEntity getWalletEntity() {
        return new WalletEntity("someId", new BigDecimal(5000));
    }


    public static String getJsonFromFile(String name) throws URISyntaxException, IOException {
        Path path = Paths.get(WalletApplicationIT.class.getClassLoader().getResource(name).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    @Configuration
    public static class TestConfiguration {

        @Bean
        public WalletRepository walletRepository() {
            return mock(WalletRepository.class);
        }

        @Bean
        public WalletHistoricRepository walletHistoricRepository() {
            return mock(WalletHistoricRepository.class);
        }

    }
}
