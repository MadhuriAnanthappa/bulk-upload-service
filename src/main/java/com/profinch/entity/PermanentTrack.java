package com.profinch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermanentTrack {
	private Integer	 slno;
	private String batchNo;
	private String drBranch;
	private String drAccount;
	private String crBranch;
	private String crAccount;
	private Double amount;
	private String txnCode;
	private String instrumentCode;
	private String narration;
	private String notifyCustomer;
}
