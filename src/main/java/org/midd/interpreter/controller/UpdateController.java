package org.midd.interpreter.controller;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.midd.interpreter.exception.MIDDControlledException;
import org.midd.interpreter.service.PersistenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateController
{
	@Resource
	private PersistenceService service;
	
	@PutMapping(value = "/{classUID}/{id}", 
			consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> update(
			@PathVariable String classUID,
			@PathVariable long id,
			@RequestBody String body)
	{
		try
		{
			return ResponseEntity.ok(
					service.updateOne(classUID, id, new JSONObject(body)).toString().concat("\n"));
		}
		catch (MIDDControlledException e) 
		{
			return ResponseEntity.ok(e.getMessage().concat("\n"));
		}
		catch (Exception e)
		{
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(e.getMessage().concat("\n"));
		}
	}
}
