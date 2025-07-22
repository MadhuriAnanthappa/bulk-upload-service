package com.profinch.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkUDF {

	private String refNo;
	private String accNo;
	private String udfField;
	private String udfVal;
	private String status;
	private String authStatus;
	private Date processedDate;
	private String userId;
	private String notifyCustomer;
}