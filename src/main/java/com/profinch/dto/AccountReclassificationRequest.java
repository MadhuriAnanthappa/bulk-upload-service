package com.profinch.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.profinch.entity.AccountReclassification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AccountReclassificationRequest {
	@NotBlank(message = "reqRefNo is mandatory")
    private String reqRefNo;      
	
	@NotNull(message = "totalRecords is required")
    private Integer totalRecords; 
	
	@NotNull(message = "accountReclassifications list is required")
	@Size(min = 1, message = "accountReclassifications list must contain at least one record")
    private List<AccountReclassification> accountReclassifications;
}