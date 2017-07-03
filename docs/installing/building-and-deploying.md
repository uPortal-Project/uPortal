# Building and deploying uPortal

## uPortal Full-Source Version

[Acquire a full copy of the source code](downloading.md).

Unpack the uPortal source outside the Tomcat container (i.e., `/usr/local/src` or any directory outside tomcat).

```
tar -xvf uPortal-4.3.0.tar.gz
```

Now, we can configure your uPortal for deployment

### Step 1: Configure the build.properties file 

The uPortal build requires a `build.properties` file describing the deployment environment to be available in the root of the uPortal project.

Go to your uPortal source directory and create the `build.properties` file by copying the `build.properties.sample` file to `build.properties`.

```shell
cp build.properties.sample build.properties
```

Open the build.properties for editing and configure the `server.home` property to point to the root directory of your Tomcat installation.

```
##### Replace server.home with the location of Tomcat 6 on your machine #####
# path to tomcat binaries
server.home=/path/to/tomcat
```

### Step 2: Configure filters

In the `filters` folder, locate the default `local.properties` file and configure the Database Connection Settings, uPortal Server Configuration Properties, CAS Server Configuration, Logging information, and LDAP settings. Replace `localhost:8080` with your server name. 

uPortal provides an optional mechanism to override these settings with a file that is completely *outside* uPortal source. This approach allows systems administrators to change these settings without rebuilding.  It also provides an easy way for adopters to keep sensitive information outside of their Source Code Management system.

```properties
## HSQL Configuration
environment.build.hsql.port=8887


## Database Connection Settings (Uncomment the Maven Filters section in rdbm.properties)
environment.build.hibernate.connection.driver_class=org.hsqldb.jdbc.JDBCDriver
environment.build.hibernate.connection.url=jdbc:hsqldb:hsql://localhost:${environment.build.hsql.port}/uPortal
environment.build.hibernate.connection.username=sa
environment.build.hibernate.connection.password=
environment.build.hibernate.dialect=org.hibernate.dialect.HSQLDialect
 
# uPortal server configuration properties
environment.build.uportal.server=localhost:8080
environment.build.uportal.protocol=http
environment.build.uportal.context=/uPortal
environment.build.uportal.email.fromAddress=portal@university.edu


# CAS server configuration properties
environment.build.cas.server=localhost:8080
environment.build.cas.protocol=http


# Log4J values applied to portlets and portals.  See log4j.properties file
# in WEB-INF or resources directory for each portlet. This provides a
# single point of control for most logging.


environment.build.log.rootLevel=INFO
# Directory to place portal and portlet log files into.
environment.build.log.logfileDirectory=${catalina.base}/logs
# Assume a DailyRollingFileAppender is used. Set the pattern to daily log-file roll-overs.
# Can also set to hourly, weekly, etc.  Use yyyy-MM-dd-HH for hourly.
# See http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/DailyRollingFileAppender.html
environment.build.log.rollingLogFileDatePattern=yyyy-MM-dd
# Pattern to specify format of each log file entry.  See http://logging.apache.org/log4j/1.2/apidocs/index.html.
environment.build.log.layoutConversionPattern=%5p [%t] %c{2}.[%x] %d{ISO8601} - %m%n
environment.build.logback.layoutConversionPattern=%-5level [%thread] %logger{36} %d{ISO8601} - %msg%n


# LDAP server connection settings (optional)
# To connect to LDAP, provide your connection information here and uncomment one 
# or both integration beans in uportal-war/src/main/resources/properties/contexts/ldapContext.xml
environment.build.ldap.url=
environment.build.ldap.baseDn=
environment.build.ldap.userName=
environment.build.ldap.password=


```

#### Optional: Creating multiple filter files per environment

By default, uPortal 4 uses the `local.properties` filter file, but you can create your own filter file and build/deploy uPortal using a flag for selecting the desired filter file. 

For example, I can create a new filter file named `test.properties` and set all my servers to point to test servers. Then, when I build and deploy uPortal I can use the flag, `-Denv=test` . Note, that you use the part of the file name before `.properties` when using the flag. This flag applies to both ant and maven. 

Make sure you use the `clean` command when making a switch between filters.

```shell
ant clean deploy-ear -Denv=test
```

```shell
mvn clean install -Denv=test
```

### Step 3: Configure your database.

Aside from entering your database server information in the `local.properties` filter file from Step 2, there are additional database configurations that may need to be performed. Find your selected database below and follow the steps for database-specific instruction:

+ DB2
+ HypersonicSQL
+ MS SQL Server and MS JDBC Driver"
+ MySQL
+ Oracle
+ PostgreSQL
+ Sybase SQL Server
+ Using JNDI managed DataSources

### Step 4: Tomcat Reminder

Just in case you missed it, don't forget to [configure Tomcat](./tomcat/), especially configuring

+ Shared libraries
+ Shared sessions
+ JVM Heap Configuration

### Step 5: Deploy uPortal

**WARNING:** Running the following command will reinitialize your database by dropping all tables first. Your content will be lost if you run against an existing database.

Run the following command to deploy uPortal and load your database

```shell
ant initportal
```


### Step 6: Restart Tomcat

```shell
$TOMCAT_HOME/bin/shutdown.sh
$TOMCAT_HOME/bin/startup.sh
```

### Step 7: Access uPortal

```
http://localhost:8080/uPortal/
```

Replace `localhost:8080` with your configured server path.

If everything has been installed correctly you should see out-of-the-box uPortal.

## uPortal Quick-Start

### Step 1: Untar the package

After downloading the uPortal quickstart version untar the package as follows

```
tar -xvf uPortal-4.2-quick-start.tar.gz

```

### Step 2: Set up `JAVA_HOME`

Make sure the `JAVA_HOME` environment variable is set

```shell
export JAVA_HOME=/path/to/java
```

### Step 3: Startup uPortal

Start uPortal by running the ant command from inside the `uPortal-4.2-quick-start` directory

```shell
# Linux
# Temporary due to https://issues.jasig.org/browse/UP-4454:
export M2_HOME=<pathToYourQuickstartLocation>/apache-maven-3.0.5
export PATH=$PATH:$M2_HOME/bin 
./ant.sh start

 
# Windows
ant start
```

On Environment Variable conflicts: If your quick start build fails, verify you do not have `CATALINA_OPTS` or `JAVA_OPTS` environment variables set with values that might cause the build or execution to fail.

### Step 4: Access uPortal

```
http://localhost:8080/uPortal/
```

If everything has been installed correctly you should see out-of-the-box uPortal.

### Step 5: Stop uPortal

```shell
ant stop
```

For detailed instructions, read the `README.txt` file located in the `uPortal-4.x-quick-start` directory.

### Ant build and deployment tasks

#### dbtest

This target will test your database configuration from the command-line (not present in quick-start).

#### initportal

The target that will deploy uPortal and load your database, but you must first set up the `JDBC` properties in `rdbm.properties` and set up the path to your servlet container in `build.properties`. 

The `initportal` target runs all the targets necessary to deploy the portal and prepare the portal database: `bootstrap`, `deploy-ear`, `db`, `pubchan`, `i18n-db`.

*IMPORTANT* Do not run this task against a database the contents of which you care about, as it initializes the database by first dropping all tables.

#### deploy-war

The `deploy-war` target first makes sure everything is compiled and up-to-date and then copies the extracted uPortal Web Application Archive (WAR) to the location required by your servlet container as specified in `build.properties`.

#### deploy-ear

The `deploy-ear` target first makes sure everything is compiled and up-to-date and builds an Enterprise Application Archive (EAR) composed of the uPortal WAR and the WARs of all the portlets being deployed. The EAR is then extracted to the location required by your servlet container as specified in `build.properties`.

#### deployPortletApp

The `deployPortletApp` target runs the portlet Deployer tool. This tool takes a portlet WAR file, rewrites the `web.xml` file and deploys the results to the servlet container.

Example: `ant deployPortletApp -DportletApp=C:/TEMP/myPortlet.war`

#### hsql

The `hsql` target starts an HSQLDB server instance consistent with the default `rdbm.properties` data access configuration of uPortal. Note that this Ant target does not return in a normal execution. The Ant "build" kicks off the database server but then just keeps on running. You'll need to kill it manually, e.g. via `control-C` or via a stop build control in your IDE.

See also [documentation about using other databases with uPortal](./database/).

## Additional references

+ [Continuous Delivery of uPortal using a RPM](https://wiki.jasig.org/display/~tefreestone@gmail.com/Continuous+Delivery+of+uPortal+using+a+RPM)
