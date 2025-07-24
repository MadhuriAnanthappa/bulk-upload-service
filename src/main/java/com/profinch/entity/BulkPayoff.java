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
    private String loanNo;

    private String settleAcc;

    private String reconNo;

    private String ccy;

    private Double amount;

    private String waive;

    private String narration;

    private String notifyCustomer;
}
