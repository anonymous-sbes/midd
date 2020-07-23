package org.midd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileUtils
{
	public List<File> findFiles (final File startingDirectory, final String pattern)
	{
		List<File> files = new ArrayList<File>();
		
		if (startingDirectory.isDirectory())
		{
			File[] sub = startingDirectory.listFiles(new FileFilter()
			{
				public boolean accept (File pathname)
				{
					return pathname.isDirectory() || pathname.getName().matches(pattern);
				}
			});
			
			for (File fileDir : sub)
			{
				if (fileDir.isDirectory())
				{
					files.addAll(findFiles(fileDir, pattern));
				}
				else
				{
					files.add(fileDir);
				}
			}
		}
		
		return files;
	}
	
	public String writeFile (byte[] content, String filename) throws IOException
	{
		String result = null;
		
		File file = new File(filename);
		
		if (!file.exists())
		{
			file.createNewFile();
		}
		else
		{
			result = "file already exist!";
		}
		
		FileOutputStream fop = new FileOutputStream(file);
		
		fop.write(content);
		
		fop.flush();
		
		fop.close();
		
		return result;
	}
	
	public String readFile (String path) throws IOException, Exception
	{
		StringBuffer buffer = new StringBuffer();
		
		BufferedReader reader = new BufferedReader(new FileReader(path));
		
		String line;
		
		while ( (line = reader.readLine()) != null)
		{
			buffer.append(line);
		}
		
		reader.close();
		
		return buffer.toString();
	}
	
	public String readFile (File file) throws IOException, Exception
	{
		StringBuffer buffer = new StringBuffer();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		String line;
		
		while ( (line = reader.readLine()) != null)
		{
			buffer.append(line);
		}
		
		reader.close();
		
		return buffer.toString();
	}
	
	public String readInputStream (InputStream is)
	{
		BufferedReader br = null;
		
		StringBuilder sb = new StringBuilder();
		
		String line;
		
		try
		{
			br = new BufferedReader(new InputStreamReader(is));
			
			while ( (line = br.readLine()) != null)
			{
				sb.append(line);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return sb.toString();
	}
		
	public String getDataFileFromClassLoader(String fileName)
	{
		String result = null;
		
		try
		{
			result = this.readInputStream(
					Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return result.toString();
	}
	
	public static String readFileToString(String filePath)
	{
		StringBuilder contentBuilder = new StringBuilder();
		
		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8))
		{
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return contentBuilder.toString();
	}
}
