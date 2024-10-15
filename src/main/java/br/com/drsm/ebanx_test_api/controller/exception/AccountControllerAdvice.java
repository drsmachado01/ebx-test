package br.com.drsm.ebanx_test_api.controller.exception;

import br.com.drsm.ebanx_test_api.service.exception.AccountNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class AccountControllerAdvice {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(AccountNotFoundException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
