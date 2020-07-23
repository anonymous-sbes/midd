package org.midd.interpreter.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.json.JSONObject;
import org.midd.interpreter.CEnv;
import org.midd.interpreter.bsttm.SQLStatements;
import org.midd.interpreter.repository.emap.RowMapper;
import org.midd.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ReadDSHImpl implements ReadDSH
{
	@Autowired
	private DataSource dataSource;
	
	public ReadDSHImpl()
	{
	}
	
	@Override
	public JSONObject findById(final String classUID, Long id) 
			throws Exception
	{
		JSONObject response = null;
		final String sqlQuery = SQLStatements.getStatements()
				.getJSONObject(classUID)
				.getJSONObject(SQLStatements.SELECT)
				.getString(SQLStatements.Zero.toString())
				.concat(Constant.where_id).concat(id.toString());
		
		try (Connection connection = dataSource.getConnection();)
		{
			try (Statement statement = connection.createStatement();)
			{
				try (ResultSet resultSet = statement.executeQuery(sqlQuery);)
				{
					response = new RowMapper().getData(classUID, null, null, 
							CEnv.getDataModel(), resultSet);
					response.put(Constant.classUID, classUID);
				}
				catch (Exception e)
				{
					throw e;
				}
			}
			catch (Exception e)
			{
				throw e;
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		
		return response;
	}
}

