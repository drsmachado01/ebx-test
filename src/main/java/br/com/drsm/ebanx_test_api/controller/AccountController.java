package br.com.drsm.ebanx_test_api.controller;

import br.com.drsm.ebanx_test_api.dto.TransactionRequestDTO;
import br.com.drsm.ebanx_test_api.dto.TransactionResponseDTO;
import br.com.drsm.ebanx_test_api.service.AccountService;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        log.info("AccountController created");
        this.service = service;
    }

    @PostMapping("/reset")
    public ResponseEntity<String> reset() {
        log.info("reset called");
        service.reset();
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance(@PathParam("account_id") String account_id) {
        log.info("getBalance called for {}", account_id);
        return new ResponseEntity<>(service.getBalance(account_id), HttpStatus.OK);
    }

    @PostMapping("/event")
    public ResponseEntity<TransactionResponseDTO> event(@RequestBody TransactionRequestDTO request) {
        log.info("event called for {}", request);
        TransactionResponseDTO transaction = switch (request.getType()) {
            case "deposit" -> service.processDeposit(request);
            case "withdraw" -> service.processWithdraw(request);
            case "transfer" -> service.processTransfer(request);
            default -> throw new IllegalArgumentException("Invalid type: " + request.getType());
        };
        log.info("event returned {}", transaction);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }
}
