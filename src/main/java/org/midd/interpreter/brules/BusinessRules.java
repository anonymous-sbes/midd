package org.midd.interpreter.brules;

import java.sql.Connection;
import java.sql.SQLException;

import org.json.JSONObject;
import org.midd.interpreter.exception.MIDDConcurrencyException;
import org.midd.interpreter.exception.MIDDControlledException;
import org.midd.interpreter.exception.MIDDDuplicatedRecordException;
import org.midd.interpreter.exception.MIDDGeneralException;

public class BusinessRules
{
	public void beforeCreate(JSONObject datas, Connection connection) throws MIDDGeneralException, 
		MIDDControlledException, 
		MIDDConcurrencyException, 
		MIDDDuplicatedRecordException, 
		SQLException, 
		Exception
	{
		
	}
	
	public void afterCreate(JSONObject datas, Connection connection) throws MIDDGeneralException, 
		MIDDControlledException, 
		MIDDConcurrencyException, 
		MIDDDuplicatedRecordException, 
		SQLException, 
		Exception
	{
		
	}
	
	public void beforeRead(JSONObject jsonObject, Connection connection) throws MIDDGeneralException, 
		MIDDControlledException, 
		MIDDConcurrencyException, 
		MIDDDuplicatedRecordException, 
		SQLException, 
		Exception
	{
		
	}
	
	public void afterRead(JSONObject jsonObject, Connection connection) throws MIDDGeneralException, 
		MIDDControlledException, 
		MIDDConcurrencyException, 
		MIDDDuplicatedRecordException, 
		SQLException, 
		Exception
	{
		
	}
	
	public void beforeUpdate(JSONObject datas, Connection connection) throws MIDDGeneralException, 
		MIDDControlledException, 
		MIDDConcurrencyException, 
		MIDDDuplicatedRecordException, 
		SQLException, 
		Exception
	{
		
	}
	
	public void afterUpdate(JSONObject datas, Connection connection) throws MIDDGeneralException, 
		MIDDControlledException, 
		MIDDConcurrencyException, 
		MIDDDuplicatedRecordException, 
		SQLException, 
		Exception
	{

	}
	
	public void beforeDelete(JSONObject datas, Connection connection) throws MIDDGeneralException, 
		MIDDControlledException, 
		MIDDConcurrencyException, 
		MIDDDuplicatedRecordException, 
		SQLException, 
		Exception
	{

	}
	
	public void afterDelete(JSONObject datas, Connection connection) throws MIDDGeneralException, 
		MIDDControlledException, 
		MIDDConcurrencyException, 
		MIDDDuplicatedRecordException, 
		SQLException, 
		Exception
	{

	}
}
