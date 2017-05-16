package com.uportal;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.hsqldb.persist.HsqlProperties;

public class HsqlDbStartTask extends DefaultTask{
	
	@TaskAction
	public void startDb() throws IOException {
		HsqlPluginExtention extension = getProject().getExtensions().findByType(HsqlPluginExtention.class);
		if (extension == null) {
			extension = new HsqlPluginExtention();
			System.out.println(String.format("Using default properties: \n %s", extension.toString()));
		}
		System.out.println("Starting Database");
		HsqlProperties p = new HsqlProperties();
		p.setProperty("server.database.0", extension.getDatabase());
		p.setProperty("server.dbname.0", extension.getDbName());
		p.setProperty("server.port", extension.getPort());
		
		HsqlDb db = new HsqlDb();
		db.startDb(p);
		
		System.out.println("Started hsql");
	}
}
