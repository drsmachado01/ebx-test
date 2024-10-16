package br.com.drsm.ebanx_test_api.service;

import br.com.drsm.ebanx_test_api.dto.AccountDTO;
import br.com.drsm.ebanx_test_api.dto.TransactionRequestDTO;
import br.com.drsm.ebanx_test_api.dto.TransactionResponseDTO;
import br.com.drsm.ebanx_test_api.model.Account;
import br.com.drsm.ebanx_test_api.service.exception.AccountNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountServiceTest {
    @Autowired
    private AccountService accountService;

    @BeforeEach
    public void init() {
        accountService.getAccountsMap().putAll(fillAccounts());
    }

    @Test
    void testReset() {
        accountService.reset();
        assertEquals(0, accountService.getAccountsMap().size());
    }

    @Test
    void testGetBalance() {
        Double bal = accountService.getBalance("123");
        assertEquals(100.0, bal);
    }

    @Test
    void testGetBalanceNotFound() {
        try {
            accountService.getBalance("-100");
            fail();
        } catch (Exception e) {
            assertInstanceOf(AccountNotFoundException.class, e);
        }
    }

    @Test
    void testCreateAccountWithInitialBalance() {
        TransactionRequestDTO request = createRequest("deposit", null, "999", 10.0);
        TransactionResponseDTO result = accountService.processDeposit(request);

        assertNotNull(result);
        assertEquals("999", result.getDestination().getId());
        assertEquals(10.0, result.getDestination().getBalance());
    }

    @Test
    void testDepositIntoExistingAccount() {
        TransactionRequestDTO request = createRequest("deposit", null, "123", 10.0);
        TransactionResponseDTO result = accountService.processDeposit(request);

        assertNotNull(result);
        assertEquals("123", result.getDestination().getId());
        assertEquals(110.0, result.getDestination().getBalance());
    }

    @Test
    void testWithdrawForNonExistingAccount() {
        TransactionRequestDTO request = createRequest("withdraw", null, "999", 10.0);
        try {
            accountService.processWithdraw(request);
            fail();
        } catch (Exception e) {
            assertInstanceOf(AccountNotFoundException.class, e);
        }
    }

    @Test
    void testWithdrawForExistingAccount() {
        TransactionRequestDTO request = createRequest("withdraw", "123", null, 10.0);
        TransactionResponseDTO result = accountService.processWithdraw(request);

        assertNotNull(result);
        assertEquals("123", result.getOrigin().getId());
        assertEquals(90.0, result.getOrigin().getBalance());
    }

    @Test
    void testTransferForNonExistingOriginAccount() {
        TransactionRequestDTO request = createRequest("transfer", "-100", "456", 10.0);
        try {
            accountService.processTransfer(request);
            fail();
        } catch (Exception e) {
            assertInstanceOf(AccountNotFoundException.class, e);
        }
    }

    @Test
    void testTransferBetweenExistingAccounts() {
        TransactionRequestDTO request = createRequest("transfer", "123", "456", 10.0);
        TransactionResponseDTO result = accountService.processTransfer(request);

        assertNotNull(result);
        assertEquals("123", result.getOrigin().getId());
        assertEquals("456", result.getDestination().getId());
        assertEquals(90.0, result.getOrigin().getBalance());
        assertEquals(110.0, result.getDestination().getBalance());
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

    private Map<String, Account> fillAccounts() {
        Map<String, Account> map = new HashMap<>();
        map.put("123", createAccount("123", 100.0));
        map.put("456", createAccount("456", 100.0));
        return map;
    }
}