package br.com.drsm.ebanx_test_api.dto;

import br.com.drsm.ebanx_test_api.model.Account;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
public class AccountDTO {
    private String id;
    private Double balance;

    public static AccountDTO from(Account saved) {
        return AccountDTO.builder()
                .id(saved.getId())
                .balance(saved.getBalance())
                .build();
    }
}
