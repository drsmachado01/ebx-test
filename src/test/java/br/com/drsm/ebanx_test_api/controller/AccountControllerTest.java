package br.com.drsm.ebanx_test_api.controller;


import br.com.drsm.ebanx_test_api.dto.AccountDTO;
import br.com.drsm.ebanx_test_api.dto.TransactionRequestDTO;
import br.com.drsm.ebanx_test_api.dto.TransactionResponseDTO;
import br.com.drsm.ebanx_test_api.model.Account;
import br.com.drsm.ebanx_test_api.service.AccountService;
import br.com.drsm.ebanx_test_api.service.exception.AccountNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AccountService accountService;

    @Test
    void testReset() {
        String url = "http://localhost:" + port + "/reset";
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals("OK", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetBalance_notFound() {
        String url = "http://localhost:" + port + "/balance?account_id=123";

        when(accountService.getBalance(anyString())).thenThrow(AccountNotFoundException.class);

        ResponseEntity<Double> response = restTemplate.getForEntity(url, Double.class);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetBalance_found() {
        String url = "http://localhost:" + port + "/balance?account_id=123";

        when(accountService.getBalance("123")).thenReturn(0.0);

        ResponseEntity<Double> response = restTemplate.getForEntity(url, Double.class);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testPostEvent_forInvalidType() {
        String url = "http://localhost:" + port + "/event";

        TransactionRequestDTO request = createRequest("invalid", null, "123", 10.0);
        TransactionResponseDTO response = createResponse(null, createAccount("123", 10.0));

        when(accountService.processDeposit(any())).thenReturn(response);

        ResponseEntity<?> responseEntity = restTemplate.postForEntity(url, request, Object.class);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void testPostEvent_forCreateAccountWithInitialBalance() {
        String url = "http://localhost:" + port + "/event";

        TransactionRequestDTO request = createRequest("deposit", null, "123", 10.0);
        TransactionResponseDTO response = createResponse(null, createAccount("123", 10.0));

        when(accountService.processDeposit(any())).thenReturn(response);

        ResponseEntity<TransactionResponseDTO> responseEntity = restTemplate.postForEntity(url, request, TransactionResponseDTO.class);

        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertEquals(response.getDestination().getId(), responseEntity.getBody().getDestination().getId());
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void testPostEvent_forExistingAccount() {
        String url = "http://localhost:" + port + "/event";

        TransactionRequestDTO request = createRequest("deposit", null, "123", 10.0);
        TransactionResponseDTO response = createResponse(null, createAccount("123", 20.0));

        when(accountService.processDeposit(any())).thenReturn(response);

        ResponseEntity<TransactionResponseDTO> responseEntity = restTemplate.postForEntity(url, request, TransactionResponseDTO.class);

        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertEquals(response.getDestination().getId(), responseEntity.getBody().getDestination().getId());
        assertNotEquals(10.0, responseEntity.getBody().getDestination().getBalance());
        assertEquals(20.0, responseEntity.getBody().getDestination().getBalance());
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void testPostEvent_forWithdrawWithNotExistingAccount() {
        String url = "http://localhost:" + port + "/event";

        TransactionRequestDTO request = createRequest("withdraw", "123", null, 10.0);
        TransactionResponseDTO response = createResponse(createAccount("123", 5.0), null);

        when(accountService.processWithdraw(any())).thenThrow(AccountNotFoundException.class);

        ResponseEntity<?> responseEntity = restTemplate.postForEntity(url, request, Object.class);

        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertEquals(0, responseEntity.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void testPostEvent_forWithdrawWithExistingAccount() {
        String url = "http://localhost:" + port + "/event";

        TransactionRequestDTO request = createRequest("withdraw", "444", null, 10.0);
        TransactionResponseDTO response = createResponse(createAccount("123", 5.0), null);

        when(accountService.processWithdraw(any())).thenReturn(response);

        ResponseEntity<TransactionResponseDTO> responseEntity = restTemplate.postForEntity(url, request, TransactionResponseDTO.class);

        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertEquals(response.getOrigin().getId(), responseEntity.getBody().getOrigin().getId());
        assertNotEquals(10.0, responseEntity.getBody().getOrigin().getBalance());
        assertEquals(5.0, responseEntity.getBody().getOrigin().getBalance());
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void testPostEvent_forTransferWithNonExistingOrigin() {
        String url = "http://localhost:" + port + "/event";

        TransactionRequestDTO request = createRequest("transfer", "444", null, 10.0);
        TransactionResponseDTO response = createResponse(createAccount("123", 5.0), null);

        when(accountService.processTransfer(any())).thenThrow(AccountNotFoundException.class);

        ResponseEntity<?> responseEntity = restTemplate.postForEntity(url, request, Object.class);

        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertEquals(0, responseEntity.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }


    @Test
    void testPostEvent_forTransfer() {
        String url = "http://localhost:" + port + "/event";

        TransactionRequestDTO request = createRequest("transfer", "444", "123", 10.0);
        TransactionResponseDTO response = createResponse(createAccount("123", 15.0), createAccount("444", 5.0));

        when(accountService.processTransfer(any())).thenReturn(response);

        ResponseEntity<TransactionResponseDTO> responseEntity = restTemplate.postForEntity(url, request, TransactionResponseDTO.class);

        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertEquals(response.getOrigin().getId(), responseEntity.getBody().getOrigin().getId());
        assertEquals(15.0, responseEntity.getBody().getOrigin().getBalance());
        assertEquals(5.0, responseEntity.getBody().getDestination().getBalance());
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    private TransactionRequestDTO createRequest(String type, String origin, String destination, Double amount) {
        return TransactionRequestDTO.builder()
                .type(type)
                .origin(origin)
                .destination(destination)
                .amount(amount)
                .build();
    }

    private TransactionResponseDTO createResponse(Account origin, Account destination) {
        var response = TransactionResponseDTO.builder()
                .build();
        if(null != origin) response.setOrigin(AccountDTO.from(origin));

        if(null != destination) response.setDestination(AccountDTO.from(destination));

        return response;
    }

    private Account createAccount(String id, double amount) {
        return Account.builder().id(id).balance(amount).build();
    }
}