package pl.tlempart.gradle.plugins.hsqldb

import java.sql.Connection 
import java.sql.DriverManager 
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HsqldbStopTask extends DefaultTask {

	private static Logger logger = LoggerFactory.getLogger(HsqldbStartTask.class);

	@TaskAction
	void stop() {
			logger.info 'Preparing for stopping hsqldb database'
			
			URLClassLoader loader = GroovyObject.class.classLoader
			project.configurations.hsqldb.each {File file ->
				logger.debug "added URL ${file.toURL()} to loader $loader"
				loader.addURL(file.toURL())
			}
 			DriverManager.registerDriver(loader.loadClass('org.hsqldb.jdbcDriver').newInstance())	
			String connectionString = "jdbc:hsqldb:hsql://${project.convention.plugins.hsqldb.address}:${project.convention.plugins.hsqldb.port}/${project.convention.plugins.hsqldb.dbname}"
			logger.debug "Connection string $connectionString" 
			Connection connection = DriverManager.getConnection(connectionString, "sa", "")
			connection.prepareStatement('shutdown').execute()
			connection.close()
			
			logger.info 'Hsqldb database was stopped successfully'		
	}
}
