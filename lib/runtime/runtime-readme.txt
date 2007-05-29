Runtime scoped dependencies will be deployed with the application (to be available at runtime) 
and will be avaiklable on the test classpath, but will not be available to the compile classpath.

log4j would be a "runtime" scope dependency but for legacy LogService's compiling against it.