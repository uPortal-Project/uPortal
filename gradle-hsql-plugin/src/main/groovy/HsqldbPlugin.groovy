package pl.tlempart.gradle.plugins.hsqldb

import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.util.GFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HsqldbPlugin implements Plugin<Project> {

	private static Logger logger = LoggerFactory.getLogger(HsqldbPlugin.class);
		
	void apply(Project project) {
		
		HsqldbPluginConvention convention = new HsqldbPluginConvention()
		project.convention.plugins.hsqldb = convention
		
		Task startServerTask = project.tasks.add("startDatabase", HsqldbStartTask.class)
		startServerTask.description = "Start HSQLDB Database"
		Task stopServerTask = project.tasks.add("stopDatabase", HsqldbStopTask.class)
		stopServerTask.description = "Stop HSQLDB Database"	
	}
}
