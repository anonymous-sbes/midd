package org.midd.interpreter.bsttm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.midd.interpreter.CEnv;
import org.midd.utils.Constant;

public class SQLStatements
{
	private static SQLStatements singleton = null;
	
	public final static Boolean Single = false;
	public final static Boolean isCollection = !Single;
	
	private static JSONObject statements = new JSONObject();
	
	public final static String CREATE = "create";
	public final static String DROP = "drop";
	public final static String INSERT = "insert";
	public final static String INSERT_ = "insert_";
	public final static String UPDATE = "update";
	public final static String UPDATE_ = "update_";
	public final static String DELETE = "delete";
	public final static String SELECT = "select";
	
	public final static Integer Zero = 10;
	public final static Integer toCountZero = 11;
	public final static Integer One = 20;
	public final static Integer toCountOne = 21;
	public final static Integer Two = 30;
	public final static Integer toCountTwo = 31;
	
	public final static String INHERITANCE = "inheritance";
	public final static String COLUMNS = "columns";
	
	public static synchronized SQLStatements getInstance ()
	{
		if (singleton == null)
		{
			singleton = new SQLStatements();
		}
		
		return singleton;
	}
	
	private void getAttributesSpecsForSpecializedForObjectSpec(final String entityName, 
			final JSONObject objectSpec, final JSONObject attributesSpecs, 
			final Set<String> entitiesNamesForRemove)
	{
		entitiesNamesForRemove.add(entityName);
		
		if (objectSpec.has(Constant._classDef) 
				&& objectSpec.getJSONObject(Constant._classDef).has(Constant._extends))
		{
			final String superEntityName = objectSpec.getJSONObject(Constant._classDef)
					.getString(Constant._extends);
			this.getAttributesSpecsForSpecializedForObjectSpec(superEntityName,
					CEnv.getDataModel().getJSONObject(superEntityName), 
					attributesSpecs, entitiesNamesForRemove);
		}
		
		final Set<String> attributesNames = objectSpec.keySet();
		attributesNames.parallelStream().forEach(attributeName ->
			attributesSpecs.put(attributeName, objectSpec.getJSONObject(attributeName))
		);
	}
	
	public JSONObject reconfigureModel(final JSONObject objectSpecs) throws Exception
	{
		final JSONObject newEntities = new JSONObject();
		final Set<String> entitiesNamesForRemove = new HashSet<String>();
		
		objectSpecs.keySet().forEach(objectSpecName -> {
			final JSONObject objectSpec = objectSpecs.getJSONObject(objectSpecName);
			if (objectSpec.getJSONObject(Constant._classDef).has(Constant._extends)) 
			{
				if (objectSpec.getJSONObject(Constant._classDef)
						.getString(Constant._specializationStrategy).equals(Constant.Join)) 
				{
					newEntities.put(objectSpecName, objectSpec);
				}
				else if (objectSpec.getJSONObject(Constant._classDef)
						.getString(Constant._specializationStrategy).equals(Constant.TablePerClass))
				{
					final JSONObject tmpObjectSpec = new JSONObject();
					final String superEntityName = objectSpec.getJSONObject(Constant._classDef)
							.getString(Constant._extends);
					this.getAttributesSpecsForSpecializedForObjectSpec(superEntityName,
							objectSpecs.getJSONObject(superEntityName), tmpObjectSpec, entitiesNamesForRemove);
					
					final Set<String> tmpAttributesSpecsForObjectSpec = tmpObjectSpec.keySet();
					tmpAttributesSpecsForObjectSpec.forEach(attributeSpecName -> {
						objectSpec.put(attributeSpecName, tmpObjectSpec.getJSONObject(attributeSpecName));
					});
					
					newEntities.put(objectSpecName, objectSpec);
				}
				else
				{
					throw new RuntimeException("Unknow class specialization strategy");
				}
			}
			else
			{
				newEntities.put(objectSpecName, objectSpec);
			}
		});
		
		entitiesNamesForRemove.forEach(entityNameForRemove -> {
			newEntities.remove(entityNameForRemove);
		});
		
		final JSONObject tmpNewEntities = new JSONObject();
		final Set<String> entitiesNames = new HashSet<String>(newEntities.keySet());
		entitiesNames.forEach(entityName -> {
			tmpNewEntities.put(entityName, new JSONObject());
			tmpNewEntities.getJSONObject(entityName).put(Constant._classDef, 
					new JSONObject(newEntities.getJSONObject(entityName).remove(Constant._classDef).toString()));
		});
		
		newEntities.keySet().forEach(entityName -> {
			newEntities.getJSONObject(entityName).keySet().forEach(attributeName -> {
				tmpNewEntities.getJSONObject(entityName).put(
						attributeName, new JSONObject(
								newEntities.getJSONObject(entityName).getJSONObject(attributeName).toString()));
			});
		});
		
		return tmpNewEntities;
	}
	
	public void build(JSONObject clazzesModel) throws JSONException, Exception
	{
		final String schema = "public";
		
		{
			/*
			 * Para limpar this.sqlStatements em caso de re-build
			 * 
			 */
			final Set<String> clazzesNames = new HashSet<String>();
			
			final Iterator<String> iterator = statements.keys();
			while (iterator.hasNext())
			{
				clazzesNames.add(iterator.next());
			}
			
			for (String clazzName : clazzesNames)
			{
				statements.remove(clazzName);
			}
		}
		
		final String sqlInsertStatement = "INSERT INTO ".concat(schema).concat(".:entityName: (:attributes:) VALUES ");
		final String sqlUpdateStatement = "UPDATE ".concat(schema).concat(".:entityName: SET :attribute=value: WHERE id=");
		final String sqlDeleteStatement = "DELETE FROM ".concat(schema).concat(".:entityName: WHERE ");
		
		final StringBuffer sqlStatementConstraints = new StringBuffer();
		
		{
			/*
			 * Bloco necessario para a pre-formatacao dos comandos SQL, classe a classe
			 * 
			 */
			final Set<String> generalizations = new HashSet<String>();
			
			Iterator<String> iterator = clazzesModel.keys();
			while (iterator.hasNext())
			{
				final String clazzName = iterator.next().toString();
				this.generalization(clazzName, clazzesModel, generalizations);
				
				if (!statements.has(clazzName))
				{
					statements.put(clazzName, new JSONObject());
					statements.getJSONObject(clazzName).put(CREATE,
							this.getSQLStatementCreateTableBy(schema,
									clazzName,
									clazzesModel,
									sqlStatementConstraints));
					statements.getJSONObject(clazzName).put(DROP,
							"DROP TABLE IF EXISTS ".concat(schema.trim().toLowerCase())
								.concat(".")
								.concat(clazzName.trim().toLowerCase())
								.concat(" CASCADE;\n"));
				}
			}
			
			iterator = clazzesModel.keys();
			while (iterator.hasNext())
			{
				final String clazzName = iterator.next().toString();
				this.generalization(clazzName, clazzesModel, generalizations);
				
				final StringBuffer sqlSelectClausula = new StringBuffer();
				final StringBuffer sqlFromClausula = new StringBuffer();
				/*
				 * IMPORTANTE: Chamdas a 'getSQLStatementSelectBy' devem ser posterior a chamdas
				 * a 'getSQLStatementCreateTableBy'
				 * 
				 */
				final Set<String> clazzesNames = new HashSet<String>();
				clazzesNames.add(clazzName);
				
				this.getSQLStatementSelectBy(schema, 
						clazzName, 
						clazzName, 
						null, 
						clazzesModel, 
						sqlSelectClausula,
						sqlFromClausula, 
						0, 
						clazzesNames, new HashMap<String, Integer>(), false);
				
				statements.getJSONObject(clazzName).put(SELECT, new JSONObject());
				
				statements.getJSONObject(clazzName).getJSONObject(SELECT)
						.put(Zero.toString(), "SELECT "
								.concat(sqlSelectClausula.substring(0, sqlSelectClausula.length() - 2).toString())
								.concat(" FROM ").concat(sqlFromClausula.toString().trim()));
				
				statements.getJSONObject(clazzName).getJSONObject(SELECT).put(toCountZero.toString(),
						"SELECT count(id) FROM ".concat(sqlFromClausula.toString().trim()));
				
				sqlSelectClausula.delete(0, sqlSelectClausula.length());
				sqlFromClausula.delete(0, sqlFromClausula.length());
				clazzesNames.clear();
				
				clazzesNames.add(clazzName);
				
				this.getSQLStatementSelectBy(schema, 
						clazzName, 
						clazzName, 
						null, 
						clazzesModel, 
						sqlSelectClausula,
						sqlFromClausula, 
						1, 
						clazzesNames, new HashMap<String, Integer>(), false);
				
				statements.getJSONObject(clazzName).getJSONObject(SELECT)
						.put(One.toString(), "SELECT "
								.concat(sqlSelectClausula.substring(0, sqlSelectClausula.length() - 2).toString())
								.concat(" FROM ").concat(sqlFromClausula.toString().trim()));
				
				statements.getJSONObject(clazzName).getJSONObject(SELECT).put(toCountOne.toString(),
						"SELECT count(id) FROM ".concat(sqlFromClausula.toString().trim()));
				
				sqlSelectClausula.delete(0, sqlSelectClausula.length());
				sqlFromClausula.delete(0, sqlFromClausula.length());
				clazzesNames.clear();
				
				clazzesNames.add(clazzName);
				
				this.getSQLStatementSelectBy(schema, 
						clazzName, 
						clazzName, 
						null, 
						clazzesModel, 
						sqlSelectClausula,
						sqlFromClausula, 
						2, 
						clazzesNames, new HashMap<String, Integer>(), false);
				
				statements.getJSONObject(clazzName).getJSONObject(SELECT)
						.put(Two.toString(), "SELECT "
								.concat(sqlSelectClausula.substring(0, sqlSelectClausula.length() - 2).toString())
								.concat(" FROM ").concat(sqlFromClausula.toString().trim()));
				
				statements.getJSONObject(clazzName).getJSONObject(SELECT).put(toCountTwo.toString(),
						"SELECT count(id) FROM ".concat(sqlFromClausula.toString().trim()));
				
				sqlSelectClausula.delete(0, sqlSelectClausula.length());
				sqlFromClausula.delete(0, sqlFromClausula.length());
				clazzesNames.clear();
				
				
				statements.getJSONObject(clazzName).put(INSERT,
						sqlInsertStatement.toString().replace(":entityName:", clazzName));
				
				statements.getJSONObject(clazzName).put(UPDATE,
						sqlUpdateStatement.toString().replace(":entityName:", clazzName.toLowerCase()));
				
				statements.getJSONObject(clazzName).put(DELETE,
						sqlDeleteStatement.toString().replace(":entityName:", clazzName));
			}
			
			iterator = clazzesModel.keys();
			while (iterator.hasNext())
			{
				final String clazzModelName = iterator.next().toString();

				final StringBuffer attributesClazzModelToUpdate = new StringBuffer();
				final StringBuffer attributesClazzModelToInsert = new StringBuffer();
				final StringBuffer values = new StringBuffer();

				int index = 1;	
				final JSONObject clazzModel = clazzesModel.getJSONObject(clazzModelName);
				final Iterator<String> attributeClazzModelIterator = clazzModel.keys();
				while (attributeClazzModelIterator.hasNext())
				{
					final String attributeClazzModelName = attributeClazzModelIterator.next().toString();
					if (attributeClazzModelName.equalsIgnoreCase(Constant._classDef)
							|| attributeClazzModelName.equalsIgnoreCase(Constant.id))
					{
						
					}
					else
					{
						clazzModel.getJSONObject(attributeClazzModelName).put("sqlType", 
								Type.getInstance()
								.getSQLTypeId(clazzModel
										.getJSONObject(attributeClazzModelName).getString(Constant.type)));
						clazzModel.getJSONObject(attributeClazzModelName).put("sqlIndex", index);
						attributesClazzModelToUpdate.append(attributeClazzModelName.trim().toLowerCase()).append("=?, ");
						attributesClazzModelToInsert.append(attributeClazzModelName.trim().toLowerCase()).append(", ");
						values.append("?, ");
						
						index++;
					}
				}
				
				statements.getJSONObject(clazzModelName).put(INSERT_,
					sqlInsertStatement.toString()
						.replace(":entityName:", clazzModelName.trim().toLowerCase())
						.replace(":attributes:", attributesClazzModelToInsert.delete(
								attributesClazzModelToInsert.length() - 2, 
									attributesClazzModelToInsert.length()).toString())
						.concat("(").concat(values.delete(
								values.length() - 2, values.length()).toString()).concat(")"));
				
				statements.getJSONObject(clazzModelName).put(UPDATE_,
						sqlUpdateStatement.toString()
							.replace(":entityName:", clazzModelName.trim().toLowerCase())
							.replace(":attribute=value:", attributesClazzModelToUpdate.delete(
									attributesClazzModelToUpdate.length() - 2, 
										attributesClazzModelToUpdate.length()).toString()));
			}
			
			statements.put(":constraints:", "\n\nDO $$\nBEGIN\n".concat(sqlStatementConstraints.toString()).concat("\nEND\n$$"));
		}
	}
	
	public static JSONObject getStatements ()
	{
		return statements;
	}
	
	public void destroy ()
	{
		final Set<String> set = new HashSet<String>();
		
		final Iterator<String> iterator = statements.keys();
		
		while (iterator.hasNext())
		{
			set.add(iterator.next());
		}
		
		for (String elemn : set)
		{
			statements.remove(elemn);
		}
	}
	
	@SuppressWarnings ("rawtypes")
	private void getSQLStatementSelectBy (String schema, 
			String clazzName, 
			String clazzNameAlias,
			String clazzNameSpecialization,
			JSONObject clazzesSpecs, 
			StringBuffer sqlSelectClausula,
			StringBuffer sqlFromClausula, 
			Integer level, 
			Set<String> clazzesNamesHierarchy, 
			Map<String, Integer> associationsClazzes, 
			Boolean specializationIsReferenced) 
					throws JSONException, JSONException, Exception
	{
		if (clazzesSpecs.getJSONObject(clazzName).getJSONObject(Constant._classDef).has(Constant._extends)) 
		{
			this.defineAlias(clazzName, clazzesSpecs, associationsClazzes);
		}
		else if (!associationsClazzes.containsKey(clazzName.trim().toLowerCase()))
		{
			associationsClazzes.put(clazzName.trim().toLowerCase(), 0);
		}
		
		final String AS = " AS ";
		StringBuffer tmp = new StringBuffer();
		tmp.append(schema).append(".").append(clazzName.trim().toLowerCase())
				.append(AS).append(clazzNameAlias.trim().toLowerCase()).append("_").append(associationsClazzes.get(clazzNameAlias.trim().toLowerCase()));
		
		sqlFromClausula.append(tmp);
		
		final JSONObject clazzSpec = clazzesSpecs.getJSONObject(clazzName);
		
		final Iterator attributeIterator = clazzSpec.keys();
		while (attributeIterator.hasNext())
		{
			final String attributeName = attributeIterator.next().toString();
			if (attributeName.equals(Constant._classDef))
			{
				if (clazzSpec.getJSONObject(Constant._classDef).has(Constant._extends))
				{
					if (!clazzesNamesHierarchy.contains(clazzSpec.getJSONObject(Constant._classDef).getString(Constant._extends)))
					{
						sqlFromClausula.append(" INNER JOIN ");
						
						final String tmpClazzName = clazzSpec.getJSONObject(Constant._classDef).getString(Constant._extends);
						
						clazzesNamesHierarchy.add(tmpClazzName);
						
						this.getSQLStatementSelectBy(schema, 
								tmpClazzName,
								tmpClazzName, 
								clazzName, 
								clazzesSpecs,
								sqlSelectClausula, 
								sqlFromClausula, 
								level, 
								clazzesNamesHierarchy, 
								associationsClazzes, 
								false);
						
						sqlFromClausula.append(" ON (").append(tmpClazzName.trim().toLowerCase()).append("_").append(associationsClazzes.get(tmpClazzName.trim().toLowerCase())).append(".id = ")
								.append(clazzName.trim().toLowerCase()).append("_").append(associationsClazzes.get(clazzName.trim().toLowerCase())).append(".id) ");
					}
				}
			}
			else if (clazzesSpecs.has(clazzSpec.getJSONObject(attributeName).getString(Constant.type)))
			{
				if (level >= 0)
				{
					Set<String> _clazzesNamesHierarchy = new HashSet<>();
					_clazzesNamesHierarchy.add(clazzName);
					
					level--;
					
					if (clazzSpec.getJSONObject(attributeName).getBoolean(Constant.isNullable))
					{
						sqlFromClausula.append(" LEFT JOIN ");
					}
					else
					{
						sqlFromClausula.append(" INNER JOIN ");
					}
					
					final String clazzNameOfAttributeClazzName = clazzSpec.getJSONObject(attributeName).getString(Constant.type);
					
					this.getSQLStatementSelectBy(schema, 
							clazzNameOfAttributeClazzName, 
							clazzNameOfAttributeClazzName,
							null, 
							clazzesSpecs, 
							sqlSelectClausula, 
							sqlFromClausula, 
							level,
							_clazzesNamesHierarchy, 
							associationsClazzes, 
							true);
					
					sqlFromClausula.append(" ON (").append(clazzNameOfAttributeClazzName.trim().toLowerCase()).append("_").append(associationsClazzes.get(clazzNameOfAttributeClazzName.trim().toLowerCase())).append(".id = ")
							.append(clazzName.trim().toLowerCase()).append("_").append(associationsClazzes.get(clazzName.trim().toLowerCase())).append(".")
							.append(attributeName.trim().toLowerCase()).append(") ");
					
					level++;
				}
			}
			else
			{
				final String alias = clazzNameAlias.trim().toLowerCase().concat("_").concat(associationsClazzes.get(clazzNameAlias.trim().toLowerCase()).toString());
				
				sqlSelectClausula
					.append(alias).append(".")
					.append(attributeName.trim().toLowerCase()).append(AS)
					.append(alias).append("_").append(attributeName).append(", ");
			}
		}
	}
	
	@SuppressWarnings ("rawtypes")
	private String getSQLStatementCreateTableBy (String schema, String clazzName, JSONObject domainSpec,
			StringBuffer sqlStatementConstraints) throws JSONException, JSONException
	{
		domainSpec.getJSONObject(clazzName).put("id",  new JSONObject("{ \"type\": \"Long\", \"isUnique\": true, \"isNullable\": false }"));
		
		JSONObject specifiedClazz = domainSpec.getJSONObject(clazzName);
		
		String idType = "bigserial";
		
		String statement = "CREATE TABLE IF NOT EXISTS ".concat(schema.trim().toLowerCase()).concat(".")
				.concat(clazzName.trim().toLowerCase()).concat(" (").concat("\n\tid :idType: NOT NULL,")
				.concat("\n\tPRIMARY KEY (id)").concat("\n);").concat(":references:").concat(":attributes:")
				.concat("\n");
		
		StringBuffer bufferForAttributes = new StringBuffer();
		
		StringBuffer bufferForReferences = new StringBuffer();
		
		Iterator attributesIterator = specifiedClazz.keys();
		
		while (attributesIterator.hasNext())
		{
			String attributeName = attributesIterator.next().toString();
			
			final JSONObject specifiedAttribute = specifiedClazz.getJSONObject(attributeName);
			final String lcClazzName = clazzName.trim().toLowerCase();
			final String lcAttributeName = attributeName.trim().toLowerCase();
			
			if (attributeName.equals(Constant._classDef))
			{
				if (specifiedAttribute.has(Constant._extends))
				{
					idType = "bigint";
					
					sqlStatementConstraints.append(Constant.SQLCommands.ADD_FK_CONSTRAINT
							.replace("{schema}", schema)
							.replace("{local_table}", lcClazzName)
							.replace("{local_key}", "id")
							.replace("{referenced_table}", specifiedAttribute.getString(Constant._extends).trim().toLowerCase())
							.replace("{referenced_key}", "id")
							.replace("{ON_UPDATE}", "CASCADE")
							.replace("{ON_DELETE}", "CASCADE")).append("\n");
				}
				else if (specifiedAttribute.has(Constant._uniqueTuple) && specifiedAttribute.getJSONArray(Constant._uniqueTuple).length() > 1)
				{
					JSONArray array = specifiedAttribute.getJSONArray(Constant._uniqueTuple);
					
					StringBuffer tmpBuffer = new StringBuffer();
					for (int index = 0; index < array.length(); index++) 
					{
						tmpBuffer.append(array.getString(index)).append(", ");
					}
					tmpBuffer.delete(tmpBuffer.length() - 2, tmpBuffer.length());
					
					sqlStatementConstraints.append(Constant.SQLCommands.ADD_UNIQUE_CONSTRAINT
							.replace("{schema}", schema)
							.replace("{local_table}", lcClazzName)
							.replace("{local_key}", lcClazzName.concat("_uniques"))
							.replace("{keys}", tmpBuffer.toString().toLowerCase())).append("\n");
				}
			}
			else if (domainSpec.has(specifiedAttribute.getString(Constant.type)))
			{
				bufferForReferences.append("\nALTER TABLE ").append(schema.trim().toLowerCase()).append(".")
						.append(clazzName.trim().toLowerCase()).append(" ADD COLUMN IF NOT EXISTS ")
						.append(attributeName.trim().toLowerCase()).append(" bigint")
						.append(specifiedAttribute.has(Constant.isNullable) ? specifiedAttribute.getBoolean(Constant.isNullable) ? " NULL" : " NOT NULL"
							: " NULL")
						.append(";");

				sqlStatementConstraints.append(Constant.SQLCommands.ADD_FK_CONSTRAINT
						.replace("{schema}", schema)
						.replace("{local_table}", lcClazzName)
						.replace("{local_key}", lcAttributeName)
						.replace("{referenced_table}", specifiedAttribute.getString(Constant.type).trim().toLowerCase())
						.replace("{referenced_key}", "id")
						.replace("{ON_UPDATE}", "CASCADE")
						.replace("{ON_DELETE}", "CASCADE")).append("\n");
			}
			else
			{
				Integer length = null;
				
				if (specifiedAttribute.has(Constant.length))
				{
					length = specifiedAttribute.getInt(Constant.length);
				}
				else
				{
					length = null;
				}
				
				bufferForAttributes.append("\nALTER TABLE ").append(schema.trim().toLowerCase()).append(".")
						.append(clazzName.trim().toLowerCase()).append(" ADD COLUMN IF NOT EXISTS ")
						.append(attributeName.trim().toLowerCase()).append(" ")
						.append(Type.getInstance().getSQLTypeName(specifiedAttribute.getString(Constant.type), length).trim()
								.toLowerCase())
						.append(specifiedAttribute.has(Constant.isNullable) ? specifiedAttribute.getBoolean(Constant.isNullable) ? " NULL" : " NOT NULL" : " NULL")
						.append(";");
				
				if (specifiedAttribute.has(Constant.isUnique) && specifiedAttribute.getBoolean(Constant.isUnique))
				{
					sqlStatementConstraints.append(Constant.SQLCommands.ADD_UNIQUE_CONSTRAINT
							.replace("{schema}", schema)
							.replace("{local_table}", lcClazzName)
							.replace("{local_key}", "id")
							.replace("{keys}", lcAttributeName)).append("\n");
				}
			}
		}
		
		return statement.replace(":idType:", idType).replace(":references:", bufferForReferences)
				.replace(":attributes:", bufferForAttributes);
	}
	
	private void generalization (String clazzName, JSONObject clazzes, Set<String> hierarchy)
			throws JSONException, Exception
	{
		JSONObject clazz = clazzes.getJSONObject(clazzName);
		if (clazz.has(Constant._classDef) && clazz.getJSONObject(Constant._classDef).has(Constant._extends))
		{
			final String genericClazzName = clazz.getJSONObject(Constant._classDef).getString(Constant._extends);
			if (hierarchy.contains(genericClazzName))
			{
				
			}
			else
			{
				hierarchy.add(genericClazzName);
				this.generalization(genericClazzName, clazzes, hierarchy);
			}
		}
	}
	
	private Integer defineAlias(String clazzSpecName, JSONObject clazzesSpecs, Map<String, Integer> associationsClazzes) throws JSONException, Exception
	{
		if (clazzesSpecs.getJSONObject(clazzSpecName).getJSONObject(Constant._classDef).has(Constant._extends))
		{
			associationsClazzes.put(clazzSpecName.trim().toLowerCase(), this.defineAlias(clazzesSpecs.getJSONObject(clazzSpecName).getJSONObject(Constant._classDef).getString(Constant._extends), clazzesSpecs, associationsClazzes));
		}
		else 
		{
			if (associationsClazzes.containsKey(clazzSpecName.trim().toLowerCase())) 
			{
				associationsClazzes.put(clazzSpecName.trim().toLowerCase(), associationsClazzes.get(clazzSpecName.trim().toLowerCase())+1);
			}
			else
			{
				associationsClazzes.put(clazzSpecName.trim().toLowerCase(), 0);
			}
		}
		
		return associationsClazzes.get(clazzSpecName.trim().toLowerCase());
	}
	
	public void updateSchema (Connection connection) throws Exception
	{
		Statement statement = null;
		
		try
		{
			final StringBuffer buffer = new StringBuffer();
			
			Iterator<String> iterator = statements.keys();
			while (iterator.hasNext())
			{
				String entityName = iterator.next();
				if (!entityName.equals(":constraints:")) 
				{
					buffer.append(statements.getJSONObject(entityName).getString(DROP));
				}
			}
			buffer.append("\n");
			iterator = statements.keys();
			while (iterator.hasNext())
			{
				String entityName = iterator.next();
				if (!entityName.equals(":constraints:")) 
				{
					buffer.append(statements.getJSONObject(entityName).getString(CREATE));
				}
			}
			buffer.append(statements.getString(":constraints:"));
			
			statement = connection.createStatement();
			statement.executeUpdate(buffer.toString());
			connection.commit();
			statement.close();
		}
		catch (SQLException e)
		{
			try
			{
				connection.rollback();
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			
			throw e;
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			// LEnv.log("SQL Statements: \n".concat(buffer.toString()));
			
			if (statement != null)
			{
				try
				{
					statement.close();
				}
				catch (SQLException e)
				{
					throw e;
				}
			}
		}
	}
}
