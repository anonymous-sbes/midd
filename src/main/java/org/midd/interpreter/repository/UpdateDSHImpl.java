package org.midd.interpreter.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

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
public class UpdateDSHImpl implements UpdateDSH
{
	@Autowired
	private DataSource dataSource;
	
	@Override
	public JSONObject updateOne(String classUID, long id, JSONObject jsonObject) 
			throws Exception
	{
		final JSONObject model = CEnv.getDataModel();
		
		try (final Connection connection = this.dataSource.getConnection();)
		{
			try
			{
				UpdateDSHImpl.this.operation(classUID, id, jsonObject, model, connection);
				
				connection.commit();
			}
			catch (SQLException e)
			{
				connection.rollback(); 
				
				if (Constant.erroCode.equals(e.getSQLState())) 
				{
					throw new SQLException(Constant.httpErrorCode.concat(e.getMessage()));
				}
				
				throw e;
			}
		}
		
		return jsonObject;
	}
	
	protected void operation(String classUID, long id, JSONObject object, 
			JSONObject model, Connection connection) throws Exception
	{
		try (PreparedStatement preparedStatement = 
				connection.prepareStatement(SQLStatements.getStatements()
						.getJSONObject(classUID).getString(SQLStatements.UPDATE_)
						.concat(String.valueOf(id)));)
		{
			JSONObject objectSpec = this.getSpec(classUID, object, model, connection);
			
			objectSpec.keySet().parallelStream().forEach(attributeNameObjectSpec -> 
			{
				try
				{
					JSONObject attributeSpec = 
							objectSpec.getJSONObject(attributeNameObjectSpec);
					if (attributeNameObjectSpec.equals(Constant.id))
					{
						
					}
					else if (attributeNameObjectSpec.equals(Constant._classDef))
					{
						
					}
					else if (object.has(attributeNameObjectSpec)) 
					{
						if (model.has(attributeSpec.getString(Constant.type))) 
						{
							JSONObject associatedObject = 
									object.getJSONObject(attributeNameObjectSpec);
							if (associatedObject.isNull(Constant.id))
							{
								UpdateDSHImpl.this.operation(
										associatedObject.remove(Constant.classUID).toString(),
										Long.parseLong(associatedObject.remove(Constant.classUID).toString()),
										associatedObject, model, connection);
							}
							
							preparedStatement.setObject(
									attributeSpec.getInt(Constant.sqlIndex), 
									associatedObject.get(Constant.id), 
									Type.SQL.Code.BIGINT);
						}
						else
						{
							preparedStatement.setObject(
									attributeSpec.getInt(Constant.sqlIndex), 
									object.get(attributeNameObjectSpec), 
									attributeSpec.getInt(Constant.sqlType));
						}
					}
					else 
					{
						preparedStatement.setObject(
								attributeSpec.getInt(Constant.sqlIndex), 
								null, Type.SQL.Code.NULL);
					}
				}
				catch (JSONException | SQLException e)
				{
					throw new RuntimeException(e.getMessage());
				}
				catch (Exception e)
				{
					throw new RuntimeException(e.getMessage());
				}
			});
			
			preparedStatement.execute();
			
			object.put("id", id);
			object.put(Constant.classUID, classUID);
		}
		catch (Exception e)
		{
			throw e;
		}
	}
	
	@Override
	public JSONArray update(JSONObject jsonObject) throws Exception
	{
		
		return null;
	}
	
	protected void getAttributesSpecs(final JSONObject objectSpec,
			final JSONObject attributesSpecs)
	{
		if (objectSpec.has(Constant._classDef) 
				&& objectSpec.getJSONObject(Constant._classDef).has(Constant._extends))
		{
			this.getAttributesSpecs(
					CEnv.getDataModel().getJSONObject(
							objectSpec.getJSONObject(Constant._classDef).getString(Constant._extends)), attributesSpecs);
		}
		
		final Set<String> attributesNames = objectSpec.keySet();
		attributesNames.parallelStream().forEach(attributeName ->
			attributesSpecs.put(attributeName, objectSpec.getJSONObject(attributeName))
		);
	}
	
	protected JSONObject getSpec(final String classUID, final JSONObject object, 
			 final JSONObject model, final Connection connection) throws Exception
	{
		final JSONObject objectSpec;
		final JSONObject tmpObjectSpec =CEnv.getDataModel().getJSONObject(classUID);
		if (tmpObjectSpec.getJSONObject(Constant._classDef).has(Constant._extends)) 
		{
			if (tmpObjectSpec.getJSONObject(Constant._classDef)
					.getString(Constant._specializationStrategy)
							.equals(Constant.TablePerClass))
			{
				objectSpec = new JSONObject();
				this.getAttributesSpecs(tmpObjectSpec, 
						objectSpec);
			}
			else if (tmpObjectSpec.getJSONObject(Constant._classDef)
					.getString(Constant._specializationStrategy)
							.equals(Constant.Join))
			{
				object.put(Constant.classUID, 
						tmpObjectSpec.getJSONObject(Constant._classDef)
							.getString(Constant._extends));
				this.operation(classUID, 0L, object, model, connection);
				object.put(Constant.classUID, classUID);
				objectSpec = tmpObjectSpec;
			}
			else
			{
				throw new RuntimeException(Constant.httpErrorCode400);
			}
		}
		else
		{
			objectSpec = tmpObjectSpec;
		}
		
		return objectSpec;
	}
}
