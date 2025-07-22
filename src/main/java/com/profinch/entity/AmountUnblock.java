package com.profinch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmountUnblock {
	private String reqRefNo;
	private String accountno;
	private Double releaseAmount;
	private String narration;
    private String uploadStatus;
    private String processStatus;
    private String notifyCustomer;

}
