package com.profinch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyTrackReceivables {
  private String loanKey;
  private String settlementAccount;
  private String amtToDeduct;
  private String narration;    
  private String notifyCustomer;

  
}
