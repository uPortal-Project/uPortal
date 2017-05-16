package com.uportal;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.hsqldb.persist.HsqlProperties;

public class HsqlDbStopTask extends DefaultTask{
	
	@TaskAction
	public void stopDb() {
		HsqlPluginExtention extension = getProject().getExtensions().findByType(HsqlPluginExtention.class);
        if (extension == null) {
            extension = new HsqlPluginExtention();
        }
        System.out.println("Stopping Database");
		HsqlProperties p = new HsqlProperties();
		p.setProperty("server.database.0", extension.getDatabase());
		p.setProperty("server.dbname.0", extension.getDbName());
		p.setProperty("server.port", extension.getPort());
		HsqlDb db = new HsqlDb();
		db.stopDb(p);
	}

}
