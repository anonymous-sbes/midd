package org.midd.interpreter.exception;

import java.io.Serializable;

public class MIDDConcurrencyException extends Exception implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MIDDConcurrencyException()
    {
        
    }
    
    public MIDDConcurrencyException(String message)
    {
        super(message);
    }
}
