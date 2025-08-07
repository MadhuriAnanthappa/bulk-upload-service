package com.profinch.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.profinch.entity.BulkRescheduling;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkReschedulingRequest {
    @NotBlank(message = "reqRefNo is mandatory")
    private String reqRefNo;
    
    @NotNull(message = "totalRecords is required")
    private int totalRecords;
    
    @NotBlank(message = "fileName is mandatory")
    private String fileName;
    
    @NotNull(message = "reschedules list is required")
    @Size(min = 1, message = "reschedules list must contain at least one record")
    private List<BulkRescheduling> reschedules;
}