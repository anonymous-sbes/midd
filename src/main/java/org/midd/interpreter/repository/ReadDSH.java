package org.midd.interpreter.repository;

import org.json.JSONObject;

public interface ReadDSH
{
	public JSONObject findById(final String classUID, final Long id) throws Exception;
}
