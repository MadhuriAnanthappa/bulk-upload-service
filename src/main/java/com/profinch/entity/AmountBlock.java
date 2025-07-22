package com.profinch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmountBlock {
	private String accountno;
	private Double amount;
	private String narration;
	private String expirydate;
	private String reqRefNo;//
    private String uploadStatus;
    private String processStatus;
    private String notifyCustomer;
}