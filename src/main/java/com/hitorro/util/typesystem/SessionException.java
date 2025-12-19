package com.hitorro.util.typesystem;

/**
 * Exception thrown when there are issues with session operations.
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 */
public class SessionException extends Exception {
    
    public SessionException() {
        super();
    }

    public SessionException(String message) {
        super(message);
    }

    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionException(Throwable cause) {
        super(cause);
    }
}
