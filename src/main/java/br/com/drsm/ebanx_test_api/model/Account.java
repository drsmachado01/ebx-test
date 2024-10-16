package br.com.drsm.ebanx_test_api.model;

import br.com.drsm.ebanx_test_api.dto.TransactionRequestDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Account {
    private String id;
    private Double balance;

    public static Account from(TransactionRequestDTO request) {
        return Account.builder()
                .id(request.getDestination())
                .balance(request.getAmount())
                .build();
    }
}
