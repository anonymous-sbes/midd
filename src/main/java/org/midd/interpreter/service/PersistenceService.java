package org.midd.interpreter.service;

import org.json.JSONObject;
import org.midd.interpreter.exception.MIDDControlledException;

public interface PersistenceService
{
	public String createOne(String classUID, JSONObject jsonObject) 
			throws MIDDControlledException, Exception;
	
	public String readById(String classUID, Long id) 
			throws MIDDControlledException, Exception;
	
	public String updateOne(String classUID, long id, JSONObject jsonObject) 
			throws MIDDControlledException, Exception;
	
	public void deleteOne(String classUID, Long id) 
			throws MIDDControlledException, Exception;
}
