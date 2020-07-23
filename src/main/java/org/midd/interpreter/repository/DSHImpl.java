package org.midd.interpreter.repository;

import java.sql.Connection;
import java.util.Set;

import org.json.JSONObject;
import org.midd.interpreter.CEnv;
import org.midd.utils.Constant;

public abstract class DSHImpl
{
	public DSHImpl()
	{
		
	}
	
	protected void getAttributesSpecsForSpecializedForObjectSpec(final JSONObject objectSpec,
			final JSONObject attributesSpecs)
	{
		if (objectSpec.has(Constant._classDef) 
				&& objectSpec.getJSONObject(Constant._classDef).has(Constant._extends))
		{
			this.getAttributesSpecsForSpecializedForObjectSpec(
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
				this.getAttributesSpecsForSpecializedForObjectSpec(tmpObjectSpec, 
						objectSpec);
			}
			else if (tmpObjectSpec.getJSONObject(Constant._classDef)
					.getString(Constant._specializationStrategy)
							.equals(Constant.Join))
			{
				object.put(Constant.classUID, 
						tmpObjectSpec.getJSONObject(Constant._classDef)
							.getString(Constant._extends));
				this.operation(object, model, connection);
				object.put(Constant.classUID, classUID);
				objectSpec = tmpObjectSpec;
			}
			else
			{
				throw new RuntimeException("400 - Unknow class specialization strategy");
			}
		}
		else
		{
			objectSpec = tmpObjectSpec;
		}
		
		return objectSpec;
	}
	
	protected abstract void operation(final JSONObject object, final JSONObject model, 
			final Connection connection) throws Exception;
}