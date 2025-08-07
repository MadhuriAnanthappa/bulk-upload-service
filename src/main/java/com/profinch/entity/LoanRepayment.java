package com.profinch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanRepayment {
    private String employeeIdentificationNumber;
    private String customerName;
    private Double deduction;
    private String reconNumber;
    private String reconBrn;
    private String deductionCode;
    private String notifyCustomer;
}