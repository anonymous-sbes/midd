package org.midd.interpreter.repository.emap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.midd.interpreter.CEnv;
import org.midd.interpreter.repository.ReadDSHImplUtils;
import org.midd.utils.Constant;

public class RowMapper
{
	public RowMapper()
	{
	}
	
	public JSONArray getData(final String classUID,
	String clazzNameAlias,
	final String specializedClazzName,
	Integer hierarchyLevel,
	final Boolean associationMode,
	final JSONObject model,
	final ResultSet resultSet) 
			throws SQLException
	{
		final JSONArray jsonArray = new JSONArray();
		try
		{
			while (resultSet.next())
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(Constant.classUID, classUID);
				new ReadDSHImplUtils().concludeRead(jsonObject, 
						clazzNameAlias, 
						specializedClazzName, 
						resultSet, 
						hierarchyLevel, 
						associationMode, 
						new HashMap<String, Integer>(),
						model);
				jsonArray.put(jsonObject);
			}
			return jsonArray;
		}
		catch (JSONException e)
		{
			CEnv.log(e);
			
			throw new RuntimeException(e.getMessage());
		}
		catch (Exception e)
		{
			CEnv.log(e);
			
			throw new RuntimeException(e.getMessage());
		}
		finally 
		{
			try
			{
				if (resultSet != null)
				{
					resultSet.close();
				}
			}
			catch (Exception ex)
			{
				
				throw new RuntimeException(ex.getMessage());
			}
		}
	}
	
	public JSONObject getData(final String classUID,
		String clazzNameAlias, final String specializedClazzName, 
		final JSONObject model, final ResultSet resultSet) throws SQLException
	{
		JSONObject jsonObject = null;
		
		try
		{
			if (resultSet.next())
			{
				jsonObject = new JSONObject();
				jsonObject.put(Constant.classUID, classUID);
				new ReadDSHImplUtils().concludeRead(jsonObject, 
						clazzNameAlias, 
						specializedClazzName, 
						resultSet, 
						new HashMap<String, Integer>(), 
						model);
			}
			else
			{
				throw new Exception("register not found!");
			}
		}
		catch (JSONException e)
		{
			CEnv.log(e);
			
			throw new RuntimeException(e.getMessage());
		}
		catch (Exception e)
		{
			CEnv.log(e);
			
			throw new RuntimeException(e.getMessage());
		}
		
		return jsonObject;
	}
}
