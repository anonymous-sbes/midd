package org.midd.interpreter.controller;

import javax.annotation.Resource;

import org.midd.interpreter.service.PersistenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteController
{
	@Resource
	private PersistenceService service;
	
	@DeleteMapping(value = "/{classUID}/{id}")
	public ResponseEntity<String> read(
			@PathVariable("classUID") String classUID, 
			@PathVariable("id") long id)
	{
		try
		{
			this.service.deleteOne(classUID, id);
			
			return ResponseEntity.ok("");
		}
		catch (Exception e)
		{
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(e.getMessage().concat("\n"));
		}
	}
}
