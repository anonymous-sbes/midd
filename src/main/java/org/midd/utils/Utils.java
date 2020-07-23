package org.midd.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

public class Utils
{
	private static final Logger log = Logger.getLogger(Utils.class.getName());

	public Utils ()
	{
		
	}
	
	public ResponseEntity<String> exceptionCatcher (Exception e, HttpStatus httpStatus)
	{
		System.gc();
		
		Runtime.getRuntime().gc();
		
		JSONObject exception = new JSONObject();
		
		try
		{
			StringWriter errors = new StringWriter();
			
			e.printStackTrace(new PrintWriter(errors));
			
			exception.put(Constant.data, new JSONObject());
			exception.getJSONObject(Constant.data).put(Constant.exception, e.getClass().getSimpleName());
			exception.getJSONObject(Constant.data).put(Constant.message, e.getMessage());
			exception.getJSONObject(Constant.data).put(Constant.stackTrace, errors.toString());
			
			if (e.getCause() != null)
			{
				exception.getJSONObject(Constant.data).put(Constant.cause, 
						String.valueOf(JsonStringEncoder.getInstance().quoteAsString(e.getCause().toString())));
			}
			else
			{
				exception.getJSONObject(Constant.data).put(Constant.cause, "");
			}
			
			log.info("EXCEPTION: GRAVE: ".concat(exception.getJSONObject(Constant.data).getString(Constant.stackTrace)));
		}
		catch (Exception ex)
		{
			log.info("EXCEPTION: CRITICAL: ".concat(ex.getMessage()));
			
			ex.printStackTrace();
		}
		
		return new ResponseEntity<String>(exception.toString(), httpStatus);
	}
	
	public String methodCanonicalName(int level)
	{
		return Thread.currentThread().getStackTrace()[level].getClassName().concat(".").concat(Thread.currentThread().getStackTrace()[level].getMethodName()).concat("(...)");
	}
	
	public static void main(String [] args)
	{
//		Set<Long> longs = new HashSet<>();
//		longs.add(1L);
//		longs.add(34L);
//		longs.add(7L);
//		longs.add(2L);
//		System.out.println(longs.toString());
		
		// 1593784117959: Fri Jul 03 10:48:37 BRT 2020
		// 1593873646856: Sat Jul 04 11:40:46 BRT 2020
		
		System.out.print(new Date(1593980342284L));
	}
}
