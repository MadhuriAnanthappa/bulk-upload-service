package com.profinch.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.profinch.entity.AmountUnblock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmountUnblockRequest {
	@NotBlank(message = "reqRefNo is mandatory")
	private String reqRefNo;
	
	@NotNull(message = "totalRecords is required")
	private int totalRecords;
	
	@NotNull(message = "amountUnblocks list is required")
	@Size(min = 1, message = "amountUnblocks list must contain at least one record")
	private List<AmountUnblock> amountUnblocks;

}
