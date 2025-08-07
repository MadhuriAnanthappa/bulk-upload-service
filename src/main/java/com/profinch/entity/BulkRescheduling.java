package com.profinch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkRescheduling {
    private String reqRefNo;
    private String loanAccount;
    private String notifyCustomer;
}