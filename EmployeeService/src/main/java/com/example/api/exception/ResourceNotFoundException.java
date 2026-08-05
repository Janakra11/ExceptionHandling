package com.example.api.exception;


import org.springframework.http.HttpStatus;

import java.net.HttpRetryException;

public class ResourceNotFoundException extends BusinessException{

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId){
        super(
                String.format("%s resource not found with id : %s", resourceType, resourceId),
                HttpStatus.NOT_FOUND,
                "Resource not found"
        );
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType(){ return resourceType; }

    public String getResourceId(){ return resourceId;}
}
