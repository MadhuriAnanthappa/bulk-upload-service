package com.profinch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkApiResponse<T> {
    private String status;    //"SUCCESS" or "FAILED"
    private String message;   // descriptive message
    private T data;           // generic data payload

}

