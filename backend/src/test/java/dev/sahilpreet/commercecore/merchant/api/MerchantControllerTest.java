package dev.sahilpreet.commercecore.merchant.api;

import dev.sahilpreet.commercecore.merchant.Merchant;
import dev.sahilpreet.commercecore.merchant.application.MerchantNotFoundException;
import dev.sahilpreet.commercecore.merchant.application.MerchantQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@WebMvcTest(MerchantController.class)
@AutoConfigureRestTestClient
public class MerchantControllerTest {

    @MockitoBean
    MerchantQueryService service;

    @Autowired
    RestTestClient client;

    @Test
    void shouldReturn200WhenMerchantExists() {
        given(service.getById(42L))
                .willReturn(new Merchant(42L, "John & Sons"));

        MerchantResponse merchant = client.get()
                .uri("/api/v1/merchants/{merchantId}", 42L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MerchantResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(merchant);
        assertEquals(42L, merchant.id());
        assertEquals("John & Sons", merchant.name());
        verify(service).getById(42L);
    }

    @Test
    void shouldReturn404WhenMerchantDoesNotExist() {
        given(service.getById(42L)).willThrow(new MerchantNotFoundException(42L));

        ProblemDetail problem = client.get()
                .uri("/api/v1/merchants/{merchantId}", 42L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(problem);
        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Merchant not found with id: 42", problem.getDetail());
        verify(service).getById(42L);
    }

    @Test
    void shouldReturn400WhenInvalidMerchantId() {
        client.get()
                .uri("/api/v1/merchants/{merchantId}", "abc")
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(service);
    }

}
