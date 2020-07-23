package org.midd.interpreter.exception;

import java.io.Serializable;

public class MIDDDuplicatedRecordException extends Exception implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 7190185206568560324L;

    public MIDDDuplicatedRecordException()
	{
		
	}
    public MIDDDuplicatedRecordException(String message)
    {
    	super(message);
	}
}
