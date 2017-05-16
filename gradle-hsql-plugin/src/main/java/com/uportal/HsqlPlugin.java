package com.uportal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HsqlPlugin implements Plugin<Project> {
	Logger logger = LoggerFactory.getLogger(HsqlPlugin.class);

	@Override
	public void apply(Project project) {
		
		project.getExtensions().create("dbSettings",HsqlPluginExtention.class);
		HsqlDbStartTask dbstartTask = project.getTasks().create("startDb", HsqlDbStartTask.class);
		dbstartTask.setDescription("Start a HSQLDB instance consistent with the default RDBMS requirements of uPortal");
		HsqlDbStopTask dbStopTask = project.getTasks().create("stopDb", HsqlDbStopTask.class);
		dbStopTask.setDescription("Compacts then cleanly shuts down hsql");
	}
}
