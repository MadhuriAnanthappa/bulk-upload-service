package com.profinch.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkPayoff {
	@NotBlank(message = "loanNo is mandatory")
    private String loanNo;

    @NotBlank(message = "settleAcc is mandatory")
    private String settleAcc;

    private String reconNo;

    @NotBlank(message = "ccy is mandatory")
    private String ccy;

    @NotNull(message = "amount is mandatory")
    @Positive(message = "amount must be greater than zero")
    private Double amout;

    private String waive;

    private String narration;

    private String notifyCustomer;
}
