package pl.tlempart.gradle.plugins.hsqldb

class HsqldbPluginConvention {

	private String address = 'localhost'
	private String port = '9001'
	private String database = 'file:test'
    private String dbname = 'test'
    private boolean silent = true
 	private boolean trace = false
 	private boolean tls = false
 	private boolean noSystemExit = false
 	private boolean remoteOpen = false
}
