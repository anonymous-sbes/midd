package org.midd.interpreter;

import java.io.Serializable;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.midd.interpreter.bsttm.SQLStatements;

@SuppressWarnings ("serial")
public class CEnv implements Serializable
{
	private static CEnv singleton;
	
	final private DataSource dataSource;
	
	static private Boolean showSQL = false;
	
	static private Boolean dropTables  = false;
	
	static private Boolean logging  = false;
	
	static private JSONObject dataModel = null;
	
	private CEnv (DataSource dataSource, Boolean showSQL, Boolean dropTables, 
			Boolean logging) throws Exception
	{
		this.dataSource = dataSource;
		
		CEnv.showSQL = showSQL;
		
		CEnv.dropTables = dropTables;
		
		CEnv.logging = logging ;
	}
	
	public static synchronized CEnv getInstance () throws JSONException
	{
		return singleton;
	}
	
	public static synchronized CEnv getInstance (DataSource dataSource,
			Boolean showSQL,  
			Boolean dropTables, 
			Boolean logging) throws Exception
	{
		if (singleton == null)
		{
			singleton = new CEnv(dataSource, showSQL, dropTables, logging);
		}
		
		return singleton;
	}
	
	static public Boolean isShowSQL()
	{
		return showSQL;
	}
	
	public static Boolean isDropTables()
	{
		return dropTables;
	}
	
	static public Boolean isLogging()
	{
		return logging;
	}
	
	public void buildSQLStatements(JSONObject dataModel) throws Exception
	{
		Connection connection = null;
		
		try
		{
			final SQLStatements sqlStatements = SQLStatements.getInstance();
			CEnv.dataModel = sqlStatements.reconfigureModel(dataModel);
			sqlStatements.build(CEnv.dataModel);
			
			connection = this.dataSource.getConnection();
			sqlStatements.updateSchema(connection);
			
			CEnv.log(SQLStatements.getStatements().toString(2));
		}
		catch (Exception e) 
		{
			throw e;
		}
		finally
		{
			if (connection != null) 
			{
				connection.close();
			}
		}
		
//		System.exit(0);
	}
	
	public DataSource getDataSource()
	{
		return this.dataSource;
	}
	
	static public JSONObject getDataModel ()
	{
		return CEnv.dataModel;
	}
	
	static public String log(Level level, String message)
	{
		if (CEnv.isLogging())
		{
			final int track = 2;
			Logger.getLogger(
					Thread.currentThread().getStackTrace()[track].getClassName().concat(".").concat(
							Thread.currentThread().getStackTrace()[track].getMethodName()).concat("()")).log(level, message);
		}
		
		return message;
	}
	
	static public String log(String message)
	{
		if (CEnv.isLogging())
		{
			final int track = 2;
			Logger.getLogger(
					Thread.currentThread().getStackTrace()[track].getClassName().concat(".").concat(
							Thread.currentThread().getStackTrace()[track].getMethodName()).concat("()")).info(message);
		}
		
		return message;
	}
	
	static public String log(Level level, Exception e)
	{
		final String exceptionMessage = e.getMessage().concat(e.getCause() == null ? "" : e.getCause().getMessage());
		
		if (CEnv.isLogging())
		{
			final int track = 2;
			Logger.getLogger(
					Thread.currentThread().getStackTrace()[track].getClassName().concat(".").concat(
							Thread.currentThread().getStackTrace()[track].getMethodName()).concat("()"))
									.log(level, exceptionMessage);
			e.printStackTrace();
		}
		
		return exceptionMessage;
	}
	
	static public String log(Exception e)
	{
		e.printStackTrace();
		final String exceptionMessage = e.getMessage().concat(e.getCause() == null ? "" : e.getCause().getMessage());
		
		if (CEnv.isLogging())
		{
			final int track = 2;
			Logger.getLogger(
					Thread.currentThread().getStackTrace()[track].getClassName().concat(".").concat(
							Thread.currentThread().getStackTrace()[track].getMethodName()).concat("()")).info(exceptionMessage);
			e.printStackTrace();
		}
		
		return exceptionMessage;
	}
}