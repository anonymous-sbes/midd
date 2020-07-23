package org.midd.interpreter.controller;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.midd.interpreter.exception.MIDDControlledException;
import org.midd.interpreter.service.PersistenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CreateController
{
	@Resource
	private PersistenceService service;
	
	@PostMapping(value = "/{classUID}", 
			consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> create(
			@PathVariable("classUID") String classUID,
			@RequestBody(required = true) String body)
	{
		try
		{
			return ResponseEntity.status(HttpStatus.CREATED).body(
					service.createOne(classUID, new JSONObject(body)).toString().concat("\n"));
		}
		catch (MIDDControlledException e) 
		{
			return ResponseEntity.status(HttpStatus.CREATED).body(e.getMessage().concat("\n"));
		}
		catch (Exception e)
		{
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(e.getMessage().concat("\n"));
		}
	}
}
