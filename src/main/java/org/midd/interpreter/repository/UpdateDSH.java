package org.midd.interpreter.repository;

import org.json.JSONArray;
import org.json.JSONObject;

public interface UpdateDSH
{
	public JSONArray update(JSONObject jsonObject) throws Exception;
	
	public JSONObject updateOne(String classUID, long id, JSONObject jsonObject) throws Exception;
}
