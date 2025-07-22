package com.profinch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCatMod {
	
	private String accountno;
	private String newCategory;
	private String reqRefNo;
    private String notifyCustomer;

}
