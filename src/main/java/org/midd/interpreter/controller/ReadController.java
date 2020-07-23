package org.midd.interpreter.controller;

import javax.annotation.Resource;

import org.midd.interpreter.exception.MIDDControlledException;
import org.midd.interpreter.service.PersistenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReadController
{
	@Resource
	private PersistenceService service;
	
	@GetMapping(value = "/{classUID}/{id}")
	public ResponseEntity<String> read(
			@PathVariable("classUID") String classUID, @PathVariable("id") long id)
	{
		try
		{
			return ResponseEntity.ok(
					service.readById(classUID, id).toString().concat("\n"));
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
