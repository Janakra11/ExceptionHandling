package com.example.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseRuntimeException extends RuntimeException{
    private ErrorCode errorCode;
    private String detailsMessage;
    private boolean showDetailsMsg= false;
    private Set<ErrorDetails> errorDetails = null;



}
