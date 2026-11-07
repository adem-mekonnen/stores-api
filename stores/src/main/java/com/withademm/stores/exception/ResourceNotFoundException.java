package com.withademm.stores.exception;

// Make sure "extends RuntimeException" is present!
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}