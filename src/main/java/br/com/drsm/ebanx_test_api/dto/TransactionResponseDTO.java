package br.com.drsm.ebanx_test_api.dto;

import br.com.drsm.ebanx_test_api.model.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponseDTO {
    private AccountDTO destination;
    private AccountDTO origin;

    public static TransactionResponseDTO fillReturn(Account origin, Account destination, String type) {
        return switch (type) {
            case "deposit" -> TransactionResponseDTO.builder()
                    .destination(AccountDTO.from(destination))
                    .origin(null)
                    .build();
            case "withdraw" -> TransactionResponseDTO.builder()
                    .destination(null)
                    .origin(AccountDTO.from(origin))
                    .build();
            case "transfer" -> TransactionResponseDTO.builder()
                    .destination(AccountDTO.from(destination))
                    .origin(AccountDTO.from(origin))
                    .build();
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
    }
}
