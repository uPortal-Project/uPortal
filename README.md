For more detailed help please refer to the [uPortal Manual](https://wiki.jasig.org/display/UPM42/Home)

Additional information about uPortal is available on the [uPortal Home Page](http://www.apereo.org/uportal)
or in the [uPortal Wiki](https://wiki.jasig.org/display/UPC/Home)

## Travis-CI Continuous Integration

uPortal uses Travis-CI for lightweight continuous integration.  You can see build statuses at [https://travis-ci.org/Jasig/uPortal].  This handy image summarizes build status for the master branch:

[![Master Branch Build Status](https://travis-ci.org/Jasig/uPortal.png?branch=master)](https://travis-ci.org/Jasig/uPortal)

## Requirements
* JDK 1.7 or later - The JRE alone is NOT sufficient, a full JDK is required
* Servlet 3.0 Container - Tomcat 7.0 or later is required.  (NOTE:  Tomcat 8 is not yet supported.)  There some configuration changes that
must be made for Tomcat which are documented in the [uPortal manual](https://wiki.jasig.org/display/UPM42/Installing+Tomcat).
* Maven 3.2.2 or later
* Ant 1.8.2 or 1.9.3 or later.

## Building and Deploying
uPortal uses Maven for its project configuration and build system. An Ant
build.xml is also provided which handles the initialization and deployment
related tasks. As a uPortal deployer you will likely only ever need to use the
Ant tasks. Ant 1.8.2 or 1.9.3 or later is required

### Ant tasks (run "ant -p" for a full list) :

* **hsql** - Starts a HSQL database instance. The default uPortal configuration points
to this database and it can be used for portal development.
* **initportal** - Runs the 'deploy-ear' and 'init-db' ant targets, should be the first
and only task run when setting up a new uPortal instance *WARNING*: This runs 'init-db' which **DROPS** and re-creates the uPortal database
* **deploy-ear** - Ensures the latest changes have been compiled and packaged then
deploys uPortal, shared libraries and all packaged portlets to the container
* **initdb** - Sets up the uPortal database. **DROPS ALL EXISTING** uPortal tables
re-creates them and populates them with the default uPortal data **WARNING**: This DROPS
and re-creates the uPortal database
* **deploy-war** - Ensures the latest uPortal changes have been compiled and packaged
then deploys the uPortal WAR to the container.
* **deployPortletApp** - Deploys the specified portlet application to the container.
This is the required process to deploy any portlet to a uPortal instance.
        ex: ant deployPortletApp -DportletApp=/path/to/portlet.war


## Help and Support
The [uportal-user@apereo.org](https://wiki.jasig.org/display/JSG/uportal-user)
email address is the best place to go with questions related to configuring or
deploying uPortal.

The uPortal manual is a collaborative document on the wiki which has more
detailed documentation: https://wiki.jasig.org/display/UPM42


## Other Notes

#### Initial Configuration
To deploy uPortal you must set the server.home variable in the
build.properties file to the instance of Tomcat you want to deploy to.


#### Build approach
The approach here is that there is a generic pom.xml and build.xml that you
should not have to edit, alongside a build.properties that you absolutely must
edit to reflect your local configuration. Edit build.properties to reflect such
configuration as where your Tomcat is, what context you would like uPortal to
be deployed as, etc.


#### Initial Deployment
You must run the initportal target before uPortal is started the first time.
This target will take care of compiling, deploying, database population and
other initial tasks. Running initportal again is similar to hitting a reset
button on the portal. Any saved configuration in the portal is lost and a clean
version of the portal is configured.

#### Logging
The /uportal-war/src/main/resources/logback.xml Logback configuration
file will end up on the classpath for Logback to find. You'll
need to either change that configuration then run deploy-war. You can configure
the logging level, where the file should be, or even choose a different logging
approach.

#### Database configuration
Database configuration is configured in /uportal-war/src/main/resources/properties/rdbm.properties
