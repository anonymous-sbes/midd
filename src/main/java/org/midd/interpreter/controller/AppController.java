package org.midd.interpreter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController
{
	@GetMapping("/StopApp")
	public ResponseEntity<?> stopApp()
	{
		try
		{
			return ResponseEntity.ok().build();
		}
		finally
		{
			System.exit(0);
		}
	}
}
