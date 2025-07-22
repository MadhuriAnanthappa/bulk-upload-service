package com.profinch.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.profinch.entity.BulkUDF;

import lombok.Data;

@Data
public class BulkUdfDTORequest {
	@NotBlank(message = "reqRefNo is mandatory")
	private String reqRefNo;
	
	@NotNull(message = "totalRecords is required")
	private int totalRecords;
	
	@NotNull(message = "bulkUdfs list is required")
	@Size(min = 1, message = "bulkUdfs list must contain at least one record")
	private List<BulkUDF> bulkUdfs;
}