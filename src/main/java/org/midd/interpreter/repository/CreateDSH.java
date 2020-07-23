package org.midd.interpreter.repository;

import org.json.JSONArray;
import org.json.JSONObject;

public interface CreateDSH
{
	public JSONArray save(String classUID, JSONObject jsonObject) throws Exception;
	
	public JSONObject saveOne(String classUID, JSONObject jsonObject) throws Exception;
}
