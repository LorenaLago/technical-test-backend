package com.playtomic.tests.wallet;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.playtomic.tests.wallet.api.WalletController;
import com.playtomic.tests.wallet.infrastructure.WalletEntity;
import com.playtomic.tests.wallet.infrastructure.WalletHistoricRepository;
import com.playtomic.tests.wallet.infrastructure.WalletRepository;
import com.playtomic.tests.wallet.service.WalletNotFoundException;
import com.playtomic.tests.wallet.service.WalletService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@SpringBootTest()
@ActiveProfiles(profiles = "test")
public class WalletApplicationIT {

    private static final int WIREMOCK_PORT = 9999;
    private static WireMockServer wireMockServer;

    @Autowired
    private WalletController walletController;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private WalletService walletService;

    @MockBean
    private WalletHistoricRepository walletHistoricRepository;



    @BeforeAll
    public static void start() {
        wireMockServer = new WireMockServer(WIREMOCK_PORT);
        wireMockServer.start();
    }

    @BeforeEach
    public void initialize() {
        reset(walletRepository, walletService, walletHistoricRepository);
        wireMockServer.resetAll();
    }

    @AfterEach
    public void stop() {
        wireMockServer.stop();
    }

    public static String getJsonFromFile(String name) throws URISyntaxException, IOException {
        Path path = Paths.get(WalletApplicationIT.class.getClassLoader().getResource(name).toURI());
        return Files.readString(path);
    }

    @Test
    public void returnAWalletForAGivenId() {
        given(this.walletRepository.findById("someId")).willReturn(Optional.of(this.getWalletEntity()));
        given().standaloneSetup(this.walletController)
                .when()
                .get("/api/wallet/someId")
                .then()
                .statusCode(HttpStatus.OK.value());

    }

    @Test
    public void returnAnErrorForANotFoundWallet() {
        given(this.walletRepository.findById("someId")).willThrow(new WalletNotFoundException());
        given().standaloneSetup(this.walletController)
                .when()
                .get("/api/wallet/someId")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void topupWallet() throws URISyntaxException, IOException {
        given(this.walletRepository.findById("someId")).willReturn(Optional.of(this.getWalletEntity()));

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
                .param("amount", BigDecimal.valueOf(5000))
                .when()
                .post("/api/wallet/someId/topUp")
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
                                        .withStatus(HttpStatus.BAD_REQUEST.value())
                                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        )
        );

        given(this.walletRepository.findById("someId")).willReturn(Optional.of(this.getWalletEntity()));
        given().standaloneSetup(this.walletController)
                .param("creditCardNumber", "411111111111")
                .param("amount", BigDecimal.ZERO)
                .when()
                .post("/api/wallet/someId/topUp")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private WalletEntity getWalletEntity() {
        return new WalletEntity("someId", BigDecimal.valueOf(5000));
    }

 }
