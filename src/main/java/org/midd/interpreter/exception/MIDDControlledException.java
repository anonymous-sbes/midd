package org.midd.interpreter.exception;

import java.io.Serializable;

public class MIDDControlledException extends Exception implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 7190185206568560324L;

    public MIDDControlledException()
    {
        
    }
    
    public MIDDControlledException(String message)
    {
        super(message);
    }
}
