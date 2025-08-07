package com.profinch.dto;

import com.profinch.entity.LoanRepayment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanRepaymentRequest {

    @NotBlank(message = "reqRefNo is mandatory")
    private String reqRefNo;
    
    @NotNull(message = "totalRecords is required")
    private int totalRecords;
    
    @NotBlank(message = "fileName is mandatory")  
    private String fileName;
    
    @NotNull(message = "loanRepayments list is required")
    @Size(min = 1, message = "loanRepayments list must contain at least one record")
    private List<LoanRepayment> loanRepayments;
}