package pl.tlempart.gradle.plugins.hsqldb

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HsqldbStartTask extends DefaultTask {

	private static Logger logger = LoggerFactory.getLogger(HsqldbStartTask.class);

	@TaskAction
	void start() {
			logger.info 'Preparing for starting hsqldb database'
			def classpath = project.configurations.hsqldb.collect { it.toURI().toURL() }.join(':')
			
			def command = """java -cp $classpath org.hsqldb.Server 
				-address $project.convention.plugins.hsqldb.address
				-port $project.convention.plugins.hsqldb.port
				-database.0 $project.convention.plugins.hsqldb.database 
				-dbname.0 $project.convention.plugins.hsqldb.dbname
				-silent $project.convention.plugins.hsqldb.silent
				-trace $project.convention.plugins.hsqldb.trace
				-tls $project.convention.plugins.hsqldb.tls
				-no_system_exit $project.convention.plugins.hsqldb.noSystemExit
				-remote_open $project.convention.plugins.hsqldb.remoteOpen"""
			
			logger.debug "Execute command: ${command}"
					
			def process = command.execute()
			
			logger.info 'Hsqldb database was started successfully'
	}
}
