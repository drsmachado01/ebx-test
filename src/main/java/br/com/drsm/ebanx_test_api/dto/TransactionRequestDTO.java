package br.com.drsm.ebanx_test_api.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TransactionRequestDTO {
    private String type;
    private String destination;
    private String origin;
    private Double amount;
}
