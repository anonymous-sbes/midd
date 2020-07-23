package org.midd.interpreter.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.midd.interpreter.bsttm.Type.YC;
import org.midd.utils.Constant;

public class ReadDSHImplUtils
{
	public Integer defineAlias(String classUID, JSONObject model, 
			Map<String, Integer> associationsIndexes) 
			throws JSONException, Exception
	{
		final JSONObject objectSpec = model.getJSONObject(classUID);
		final String lcClassUID = classUID.trim().toLowerCase();
		if (objectSpec.getJSONObject(Constant._classDef).has(Constant._extends))
		{
			associationsIndexes.put(lcClassUID, 
					this.defineAlias(objectSpec.getJSONObject(Constant._classDef)
							.getString(Constant._extends), model, associationsIndexes));
		}
		else
		{
			if (associationsIndexes.containsKey(lcClassUID)) 
				associationsIndexes.put(lcClassUID,associationsIndexes.get(lcClassUID)+1);
			else
				associationsIndexes.put(lcClassUID, 0);
		}
		
		return associationsIndexes.get(lcClassUID);
	}
	
	public void concludeRead (final JSONObject object, 
			String auxClassUID, 
			final String specializedClassUID,
			final ResultSet resultSet, 
			Integer hierarchyLevel, 
			final Boolean associationMode, 
			final Map<String, Integer> associationsIndexes,
			final JSONObject model) throws SQLException, JSONException, Exception
	{
		final String classUID = object.remove(Constant.classUID).toString().trim();
		final String lcClassUID = classUID.toLowerCase();
		
		if (auxClassUID == null)
		{
			auxClassUID = classUID;
		}
		
		Boolean isHierarchyTop = false;
		
		if (model.getJSONObject(classUID).getJSONObject(Constant._classDef).has(Constant._extends)) 
		{
			this.defineAlias(classUID, model, associationsIndexes);
		}
		else 
		{
			if (specializedClassUID != null) 
			{
				isHierarchyTop = true;
			}
			
			if (!associationsIndexes.containsKey(lcClassUID))
			{
				associationsIndexes.put(lcClassUID, 0);
			}
		}
		
		final JSONObject objectSpec = model.getJSONObject(classUID);
		
		final Iterator<String> iterator = objectSpec.keys();
		while (iterator.hasNext())
		{
			final String attributeSpecName = iterator.next();
			
			final JSONObject attributeSpec = objectSpec.getJSONObject(attributeSpecName);
			
			if (attributeSpecName.equals(Constant._classDef))
			{
				if (objectSpec.getJSONObject(Constant._classDef).has(Constant._extends))
				{
					object.put(Constant.classUID, 
							objectSpec.getJSONObject(Constant._classDef).getString(Constant._extends));
					
					this.concludeRead(object, null, classUID, resultSet, hierarchyLevel, 
							false, associationsIndexes, model);
					
					object.put(Constant.classUID, classUID);
				}
			}
			else if (isHierarchyTop && (attributeSpecName.trim().equals(Constant.id)))
			{
				
			}
			else if (model.has(attributeSpec.getString(Constant.type)))
			{
				if (hierarchyLevel > 0 && associationMode)
				{
					if (!object.has(attributeSpecName))
					{
						object.put(attributeSpecName, new JSONObject());
						object.getJSONObject(attributeSpecName).put(Constant.classUID, 
								attributeSpec.getString(Constant.type));
					}
					
					hierarchyLevel--;
					this.concludeRead(object.getJSONObject(attributeSpecName), 
							attributeSpec.getString(Constant.type), null, resultSet, 
							hierarchyLevel, associationMode, associationsIndexes, model);
					hierarchyLevel++;
				}
			}
			else
			{
				final String lcAuxClassUID = auxClassUID.trim().toLowerCase();
				final String symbol = "_".concat(associationsIndexes.get(lcAuxClassUID).toString());
				switch (attributeSpec.getString(Constant.type))
				{
					case YC.String:
					{
						object.put(attributeSpecName, 
								resultSet.getString(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Date:
					{
						object.put(attributeSpecName, 
								resultSet.getDate(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Boolean:
					{
						object.put(attributeSpecName, 
								resultSet.getBoolean(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Time:
					{
						object.put(attributeSpecName, 
								resultSet.getTime(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Long:
					{
						object.put(attributeSpecName, 
								resultSet.getLong(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Integer:
					{
						object.put(attributeSpecName, 
								resultSet.getInt(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Double:
					{
						object.put(attributeSpecName, 
								resultSet.getDouble(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Float:
					{
						object.put(attributeSpecName, 
								resultSet.getFloat(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Image:
					{
						object.put(attributeSpecName, 
								resultSet.getString(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.PDF:
					{
						object.put(attributeSpecName, 
								resultSet.getString(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Binary:
					{
						object.put(attributeSpecName, 
								resultSet.getString(auxClassUID.trim().toLowerCase().concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					default:
					{
						throw new Exception("Object attribute type unknow");
					}
				}
			}
		}
		
		object.put(Constant.classUID, classUID);
	}
	
	public void concludeRead (final JSONObject object, 
			String auxClassUID, 
			final String specializedClassUID,
			final ResultSet resultSet,  
			final Map<String, Integer> associationsIndexes,
			final JSONObject model) throws SQLException, JSONException, Exception
	{
		final String classUID = object.remove(Constant.classUID).toString().trim();
		final String lcClassUID = classUID.toLowerCase();
		
		if (auxClassUID == null)
		{
			auxClassUID = classUID;
		}
		
		Boolean isHierarchyTop = false;
		
		if (model.getJSONObject(classUID).getJSONObject(Constant._classDef).has(Constant._extends)) 
		{
			this.defineAlias(classUID, model, associationsIndexes);
		}
		else 
		{
			if (specializedClassUID != null) 
			{
				isHierarchyTop = true;
			}
			
			if (!associationsIndexes.containsKey(lcClassUID))
			{
				associationsIndexes.put(lcClassUID, 0);
			}
		}
		
		final JSONObject objectSpec = model.getJSONObject(classUID);
		
		final Iterator<String> iterator = objectSpec.keys();
		while (iterator.hasNext())
		{
			final String attributeSpecName = iterator.next();
			
			final JSONObject attributeSpec = objectSpec.getJSONObject(attributeSpecName);
			
			if (attributeSpecName.equals(Constant._classDef))
			{
				if (objectSpec.getJSONObject(Constant._classDef).has(Constant._extends))
				{
					object.put(Constant.classUID, 
							objectSpec.getJSONObject(Constant._classDef).getString(Constant._extends));
					
					this.concludeRead(object, null, classUID, resultSet, 
							associationsIndexes, model);
					
					object.put(Constant.classUID, classUID);
				}
			}
			else if (isHierarchyTop && (attributeSpecName.trim().equals(Constant.id)))
			{
				
			}
			else if (model.has(attributeSpec.getString(Constant.type)))
			{
				
			}
			else
			{
				final String lcAuxClassUID = auxClassUID.trim().toLowerCase();
				final String symbol = "_".concat(associationsIndexes.get(lcAuxClassUID).toString());
				switch (attributeSpec.getString(Constant.type))
				{
					case YC.String:
					{
						object.put(attributeSpecName, 
								resultSet.getString(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Date:
					{
						object.put(attributeSpecName, 
								resultSet.getDate(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Boolean:
					{
						object.put(attributeSpecName, 
								resultSet.getBoolean(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Time:
					{
						object.put(attributeSpecName, 
								resultSet.getTime(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Long:
					{
						object.put(attributeSpecName, 
								resultSet.getLong(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Integer:
					{
						object.put(attributeSpecName, 
								resultSet.getInt(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Double:
					{
						object.put(attributeSpecName, 
								resultSet.getDouble(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Float:
					{
						object.put(attributeSpecName, 
								resultSet.getFloat(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Image:
					{
						object.put(attributeSpecName, 
								resultSet.getString(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.PDF:
					{
						object.put(attributeSpecName, 
								resultSet.getString(lcAuxClassUID.concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					case YC.Binary:
					{
						object.put(attributeSpecName, 
								resultSet.getString(auxClassUID.trim().toLowerCase().concat(symbol).concat("_").concat(attributeSpecName.trim().toLowerCase())));
						break;
					}
					default:
					{
						throw new Exception("Object attribute type unknow");
					}
				}
			}
		}
		
		object.put(Constant.classUID, classUID);
	}
}
