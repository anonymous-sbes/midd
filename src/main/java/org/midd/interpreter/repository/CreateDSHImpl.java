package org.midd.interpreter.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.midd.interpreter.CEnv;
import org.midd.interpreter.bsttm.SQLStatements;
import org.midd.interpreter.bsttm.Type;
import org.midd.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CreateDSHImpl extends DSHImpl implements CreateDSH
{
	@Autowired
	private DataSource dataSource;
	
	@Override
	public JSONObject saveOne(String classUID, JSONObject jsonObject) throws Exception
	{
		final JSONObject model = CEnv.getDataModel();
		
		try (final Connection connection = this.dataSource.getConnection();)
		{
			try
			{
				jsonObject.put(Constant.classUID, classUID);
				jsonObject.remove(Constant.id);
				
				CreateDSHImpl.this.operation(jsonObject, model, connection);
				
				connection.commit();
			}
			catch (SQLException e) 
			{
				connection.rollback(); 
				
				if ("23505".equals(e.getSQLState())) 
				{
					throw new SQLException("409 - ".concat(e.getMessage()));
				}
				
				throw e;
			}
		}
		
		return jsonObject;
	}
	
	@Override
	protected void operation(final JSONObject object, final JSONObject model,
			final Connection connection) throws Exception
	{
		final String classUID = object.remove(Constant.classUID).toString();
		
		try (PreparedStatement preparedStatement = 
				connection.prepareStatement(SQLStatements.getStatements()
						.getJSONObject(classUID).getString(SQLStatements.INSERT_), 
								Statement.RETURN_GENERATED_KEYS);)
		{
			final JSONObject objectSpec = super.getSpec(
					classUID, object, model, connection);
			
			objectSpec.keySet().parallelStream().forEach(attributeNameObjectSpec -> 
			{
				final JSONObject attributeSpec = 
						objectSpec.getJSONObject(attributeNameObjectSpec);
				
				if (object.has(attributeNameObjectSpec)) 
				{
					if (model.has(attributeSpec.getString(Constant.type))) 
					{
						final JSONObject associatedObject = 
								object.getJSONObject(attributeNameObjectSpec);
						if (associatedObject.isNull(Constant.id))
						{
							try
							{
								CreateDSHImpl.this.operation(associatedObject, model, connection);
							}
							catch (Exception e)
							{
								throw new RuntimeException(e);
							}
						}
						
						try
						{
							preparedStatement.setObject(
									attributeSpec.getInt(Constant.sqlIndex), 
									associatedObject.get(Constant.id), 
									Type.SQL.Code.BIGINT);
						}
						catch (JSONException | SQLException e)
						{
							e.printStackTrace(); throw new RuntimeException(e);
						}
					}
					else
					{
						try
						{
							preparedStatement.setObject(
									attributeSpec.getInt(Constant.sqlIndex), 
									object.get(attributeNameObjectSpec), 
									attributeSpec.getInt(Constant.sqlType));
						}
						catch (JSONException | SQLException e)
						{
							e.printStackTrace(); throw new RuntimeException(e);
						}
					}
				}
				else if (attributeNameObjectSpec.equals(Constant._classDef))
				{
					
				}
				else if (attributeNameObjectSpec.equals(Constant.id))
				{
					
				}
				else
				{
					try
					{
						preparedStatement.setObject(
								attributeSpec.getInt(Constant.sqlIndex), 
								null, Type.SQL.Code.NULL);
					}
					catch (JSONException | SQLException e)
					{
						e.printStackTrace(); throw new RuntimeException(e);
					}
				}
			});
			
			preparedStatement.executeUpdate();
			
			ResultSet generatedKey = preparedStatement.getGeneratedKeys();
			if (generatedKey == null)
			{
				throw new SQLException("[ERROR] can not get generatedd entity key");
			}
			else if (generatedKey.next())
			{
				object.put(Constant.id, generatedKey.getLong(1));
				object.put(Constant.classUID, classUID);
			}
			else
			{
				throw new SQLException("[ERROR] can not get generated entity key");
			}
		}
		catch (Exception e)
		{
			throw e;
		}
	}
	
	@Override
	public JSONArray save(String classUID, JSONObject jsonObject) throws Exception
	{
		return null;
	}
}
