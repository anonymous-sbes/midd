package org.midd.interpreter.bsttm;

import java.sql.Types;
import java.util.Date;

import org.json.JSONException;

public class Type
{
	private static Type singleton;
	
	private Type ()
	{
		
	}
	
	public static synchronized Type getInstance ()
	{
		if (singleton == null)
		{
			singleton = new Type();
		}
		
		return singleton;
	}
	
	public interface SQL
	{
		public static String _varchar = "varchar";
		
		public static String _text = "text";
		
		public static String _int = "int";
		
		public static String _bigint = "bigint";
		
		public static String _bigserial = "bigserial";
		
		public static String _time = "time";
		
		public static String _double_precision = "double precision";
		
		public static String _real = "real";
		
		public static String _boolean = "boolean";
		
		public static String _date = "date";
		
		public static String _timestamp = "timestamp";
		
		public interface Code
		{
			public static int VARCHAR = Types.VARCHAR;
			
			public static int INTEGER = Types.INTEGER;
			
			public static int BIGINT = Types.BIGINT;
			
			public static int TIME = Types.TIME;
			
			public static int DOUBLE = Types.DOUBLE;
			
			public static int FLOAT = Types.FLOAT;
			
			public static int BOOLEAN = Types.BOOLEAN;
			
			public static int DATE = Types.DATE;
			
			public static int TIMESTAMP = Types.TIMESTAMP;
			
			public static int NULL = Types.NULL;
		}
	}
	
	public interface YC
	{
		public static String String = "String";
		
		public static String Integer = "Integer";
		
		public static String Long = "Long";
		
		public static String Double = "Double";
		
		public static String Float = "Float";
		
		public static String Boolean = "Boolean";
		
		public static String Date = "Date";
		
		public static String Timestamp = "Timestamp";
		
		public static String Time = "Time";
		
		public static String Image = "Image";
		
		public static String PDF = "PDF";
		
		public static String Binary = "Binary";
	}
	
	public String getSQLTypeName (String type, Integer length) throws JSONException
	{
		switch (type)
		{
			case YC.String:
			{
				return (length == null || length <= 0) ? SQL._text : SQL._varchar.concat("(").concat(String.valueOf(length)).concat(")");
			}
			case YC.Date:
			{
				return SQL._date;
			}
			case YC.Boolean:
			{
				return SQL._boolean;
			}
			case YC.Time:
			{
				return SQL._time;
			}
			case YC.Timestamp:
			{
				return SQL._timestamp;
			}
			case YC.Long:
			{
				return SQL._bigint;
			}
			case YC.Integer:
			{
				return SQL._int;
			}
			case YC.Double:
			{
				return SQL._double_precision;
			}
			case YC.Float:
			{
				return SQL._real;
			}
			case YC.Image:
			{
				return SQL._text;
			}
			case YC.PDF:
			{
				return SQL._text;
			}
			case YC.Binary:
			{
				return SQL._text;
			}
			default:
			{
				
				return null;
			}
		}
	}
	
	public Integer getSQLTypeId (String ycType)
	{
		switch (ycType)
		{
			case YC.String:
			{
				return Types.VARCHAR;
			}
			case YC.Date:
			{
				return Types.DATE;
			}
			case YC.Boolean:
			{
				return Types.BOOLEAN;
			}
			case YC.Time:
			{
				return Types.TIMESTAMP;
			}
			case YC.Long:
			{
				return Types.BIGINT;
			}
			case YC.Integer:
			{
				return Types.INTEGER;
			}
			case YC.Double:
			{
				return Types.DOUBLE;
			}
			case YC.Float:
			{
				return Types.REAL;
			}
			case YC.Image:
			{
				return Types.BLOB;
			}
			case YC.PDF:
			{
				return Types.BLOB;
			}
			case YC.Binary:
			{
				return Types.BLOB;
			}
			default:
			{
				
				return null;
			}
		}
	}
	
	public Boolean isYCCanonicalType (String type)
	{
		switch (type)
		{
			case YC.String:
			{
				return true;
			}
			case YC.Date:
			{
				return true;
			}
			case YC.Boolean:
			{
				return true;
			}
			case YC.Time:
			{
				return true;
			}
			case YC.Long:
			{
				return true;
			}
			case YC.Integer:
			{
				return true;
			}
			case YC.Double:
			{
				return true;
			}
			case YC.Float:
			{
				return true;
			}
			default:
			{
				
				return false;
			}
		}
	}
	
	synchronized public String getValueFormattedByType (Object value, String type) throws Exception
	{
		if (value == null) {
			return null;
		}
		
		try
		{
			switch (type)
			{
				case YC.String:
				{
					return "'".concat(value.toString().trim()).concat("'");
				}
				case YC.Date:
				{
					return "'".concat(java.sql.Date.valueOf(value.toString()).toString()).concat("'");
				}
				case YC.Timestamp:
				{
					return "'".concat(String.valueOf(new java.sql.Timestamp(new Date().getTime()))).concat("'");
				}
				default:
				{
					
					return value.toString();
				}
			}
		}
		catch (Exception e)
		{
			throw new Exception("Type conversion error");
		}
	}
}
