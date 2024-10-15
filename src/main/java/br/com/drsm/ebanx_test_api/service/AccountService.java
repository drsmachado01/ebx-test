package br.com.drsm.ebanx_test_api.service;

import br.com.drsm.ebanx_test_api.dto.TransactionRequestDTO;
import br.com.drsm.ebanx_test_api.dto.TransactionResponseDTO;
import br.com.drsm.ebanx_test_api.model.Account;
import br.com.drsm.ebanx_test_api.service.exception.AccountNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
public class AccountService {

    private final Map<String, Account> accountsMap = new HashMap<>();

    public void reset() {
        log.info("reset called");
        log.info("accountsMap size: {}", accountsMap.size());
        accountsMap.clear();
        log.info("accountsMap size: {}", accountsMap.size());
        log.info("reset successful");
    }

    public Account getAccount(String id) {
        log.info("getAccount called for {}", id);
        return accountsMap.get(id);
    }

    public void saveAccount(Account account) {
        log.info("saveAccount called for {}", account.getId());
        accountsMap.put(account.getId(), account);
    }

    public Double getBalance(String id) {
        log.info("getBalance called for {}", id);
        Account acc = getAccount(id);
        if (null == acc) {
            log.warn("Account not found: {}", id);
            throw new AccountNotFoundException(id);
        }
        return acc.getBalance();
    }

    public TransactionResponseDTO processTransfer(TransactionRequestDTO request) {

        Account origin = null, destination = null;

        origin = getAccount(request.getOrigin());
        if(null == origin) {
            log.info("Account not found: {}", request.getOrigin());
            throw new AccountNotFoundException(request.getOrigin());
        }
        log.info("origin: {}", origin);

        destination = getAccount(request.getDestination());
        if(null == destination) {
            log.info("Creating destination account");
            destination = Account.builder().id(request.getDestination()).balance(0.0).build();
        }


        origin.setBalance(origin.getBalance() - request.getAmount());
        log.info("origin balance: {}", origin.getBalance());
        destination.setBalance(destination.getBalance() + request.getAmount());
        log.info("destination balance: {}", destination.getBalance());

        return TransactionResponseDTO.fillReturn(origin, destination, request.getType());
    }

    public TransactionResponseDTO processDeposit(TransactionRequestDTO request) {
        String id = request.getDestination();
        double amount = request.getAmount();

        Account destination = getAccount(id);
        if (null == destination || null == destination.getId()) {
            log.info("Creating destination account");
            destination = Account.builder().id(id).balance(0.0).build();
        }

        destination.setBalance(destination.getBalance() + amount);
        saveAccount(destination);
        log.info("Destination created: {}", destination);
        return TransactionResponseDTO.fillReturn(null, destination, request.getType());
    }

    public TransactionResponseDTO processWithdraw(TransactionRequestDTO request) {
        String id = request.getOrigin();
        double amount = request.getAmount();
        Account origin = getAccount(id);
        if (null == origin) {
            log.info("Account not found: {}", id);
            throw new AccountNotFoundException(id);
        }
        origin.setBalance(origin.getBalance() - amount);
        saveAccount(origin);
        log.info("origin balance: {}", origin.getBalance());
        return TransactionResponseDTO.fillReturn(origin, null, request.getType());
    }


}
