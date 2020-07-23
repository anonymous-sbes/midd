package org.midd.interpreter;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.json.JSONObject;
import org.midd.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class MIDDService implements CommandLineRunner
{
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private Environment environment;
	
	public static void main(String... args) throws Exception
	{
		new SpringApplication(MIDDService.class).run(args);
	}
	
	@Override
	public void run(String... args) throws Exception
	{
		try
		{
			CEnv.getInstance(this.dataSource, 
					Boolean.valueOf(this.environment.getProperty("midd.sql_show")), 
					Boolean.valueOf(this.environment.getProperty("midd.sql_drop_tables")), 
					Boolean.valueOf(this.environment.getProperty("midd.logging")));
			
			Boolean onBoot = 
					Boolean.valueOf(this.environment.getProperty("midd.onboot"));
			
			CEnv.log("build statements on ".concat(onBoot ? "startup" : "the fly"));
			
			if (onBoot) 
			{
				CEnv.getInstance().buildSQLStatements(new JSONObject(
						FileUtils.readFileToString("/tmp/dmodel.json")));
				
				CEnv.log("SQL Statemens installed ... Ok");
			}
			
			CEnv.log("LEnv installed ... Ok");
			
			Thread.sleep(1*1000);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			System.exit(-1);
		}
	}
	
	@PreDestroy
	public void onExit()
	{
		
	}
}