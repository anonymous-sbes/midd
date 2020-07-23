package org.midd.interpreter.service;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.midd.interpreter.exception.MIDDControlledException;
import org.midd.interpreter.repository.CreateDSH;
import org.midd.interpreter.repository.DeleteDSH;
import org.midd.interpreter.repository.ReadDSH;
import org.midd.interpreter.repository.UpdateDSH;
import org.springframework.stereotype.Service;

@Service
public class PersistenceServiceImpl implements PersistenceService 
{
	@Resource
	public CreateDSH createDSH;
	
	@Resource
	public ReadDSH readDSH;
	
	@Resource
	public UpdateDSH updateDSH;
	
	@Resource
	public DeleteDSH deleteDSH;
	
	@Override
	public String createOne(String classUID, JSONObject jsonObject) 
			throws MIDDControlledException, Exception
	{
		return this.createDSH.saveOne(classUID, jsonObject).toString();
	}
	
	@Override
	public String readById(String classUID, Long id) 
			throws MIDDControlledException, Exception
	{
		return this.readDSH.findById(classUID, id).toString();
	}
	
	@Override
	public String updateOne(String classUID, long id, JSONObject jsonObject) 
			throws MIDDControlledException, Exception
	{
		return this.updateDSH.updateOne(classUID, id, jsonObject).toString();
	}
	
	@Override
	public void deleteOne(String classUID, Long id) 
			throws MIDDControlledException, Exception
	{
		this.deleteDSH.deleteOne(classUID, id);
	}
}
