package com.profinch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.profinch.entity.AmountBlock;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AmountBlockRequest {
	@NotBlank(message = "reqRefNo is mandatory")
	private String reqRefNo;
	
	@NotNull(message = "totalRecords is required")
	private int totalRecords;
	
	@NotNull(message = "amountBlocks list is required")
	@Size(min = 1, message = "amountBlocks list must contain at least one record")
	private List<AmountBlock> amountBlocks;
}
