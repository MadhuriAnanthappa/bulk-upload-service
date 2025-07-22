package com.profinch.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.profinch.entity.RefundProcess;

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
public class RefundProcessRequest {
	@NotBlank(message = "reqRefNo is mandatory")
	private String reqRefNo;
	
	@NotNull(message = "totalRecords is required")
	private int totalRecords;
	
	@NotNull(message = "refundProcesses list is required")
	@Size(min = 1, message = "refundProcesses list must contain at least one record")
	private List<RefundProcess> refundProcesses;
}
