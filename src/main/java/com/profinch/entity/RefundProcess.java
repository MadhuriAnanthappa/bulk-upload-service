package com.profinch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundProcess {
	private String empNo;
	private String name;
	private double debitAmount;
	private String status;
	private String errorCode;
	private String acDesc;
	private String custNo;
	private String refundAccount;
	private String accountNo;
	private String employer;
	private String reconNo;
	private String reconBrn;
	private String excessGL;
	private String narration;
	private String notifyCustomer;
	private String txtBrn;

}
