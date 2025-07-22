package com.profinch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountReclassification {
    private String accountNo;     
    private String newAcCls;      
    private String smsRequired;  
    private String onlineChargePlanString;
    private String offlineChargePlanString;
    private String notifyCustomer;
}
