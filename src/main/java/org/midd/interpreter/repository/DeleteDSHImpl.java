package org.midd.interpreter.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.json.JSONObject;
import org.midd.interpreter.bsttm.SQLStatements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DeleteDSHImpl extends DSHImpl implements DeleteDSH
{
	@Autowired
	private DataSource dataSource;
	
	@Override
	public String deleteOne(String classUID, Long id) throws Exception
	{
		try (final Connection connection = this.dataSource.getConnection();)
		{
			try (final Statement statement = connection.createStatement();)
			{
				statement.execute(SQLStatements.getStatements()
						.getJSONObject(classUID).getString(SQLStatements.DELETE)
								.concat(" id = ").concat(id.toString()));
				
				connection.commit();
			}
			catch (SQLException e) 
			{
				connection.rollback(); throw e;
			}
		}
		
		return "";
	}
	
	@Override
	protected void operation(JSONObject object, JSONObject model, Connection connection) throws Exception
	{
		// TODO Auto-generated method stub
		
	}
}
