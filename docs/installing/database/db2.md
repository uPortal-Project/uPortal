# Using uPortal with DB2

## Step 1: Obtain the Driver

Since the DB2 JDBC driver is not available in the central Maven repository, it must be placed into the local repository of each machine on which you wish to build uPortal.

As an alternative to this, you could set up a maven repository for use by multiple machines.

A JDBC DB2 driver is included in the DB2 software in the `java` subdirectory after DB2 installation has been performed. To install the JAR into your local maven repository, use the following command:

```
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2-jdbc -Dversion=<version> -Dpackaging=jar -DgeneratePom=true -Dfile=db2java.zip.jar
```


The `groupId`, `artifactId` and `version` specified in this command are up to you, but they should match the JAR vendor, name and version to avoid confusion down the road.

## Step 2: Configure the Database Filter

In the filters folder, locate the default `local.properties` file under `uPortal-4.1.x/filters/local.properties` and configure the Database Connection Settings

```shell
# HSQL Configuration
environment.build.hsql.port=8887

# Database Connection Settings 
environment.build.hibernate.connection.driver_class=COM.ibm.db2.jdbc.app.DB2Driver
environment.build.hibernate.connection.url=jdbc:db2:uPortal3Db
environment.build.hibernate.connection.username=sa
environment.build.hibernate.connection.password=
environment.build.hibernate.dialect=org.hibernate.dialect.DB2Dialect
```

## Step 3: Add the database driver 

Open `uportal-db/pom.xml` file, uncomment the db2 driver below and modify as needed.

Add the appropriate version properties to the root `pom.xml` file or enter the appropriate version below

```xml
<dependencies>
  <!-- Add any db drivers that are applicable to *any* of your environments -->
  <dependency>
    <groupId>org.hsqldb</groupId>
    <artifactId>hsqldb</artifactId>
    <version>${hsqldb.version}</version>
    <scope>compile</scope>
  </dependency>
  <!--
   | The following db drivers should be uncommented and/or modified as needed for server 
   | deployments.  (Add all thaat are needed.)  Don't forget to add appropriate  .version 
   | properties to the root pom.xml, or simply enter the appropriate version below.
   +-->
  <!--
  <dependency>
    <groupId>postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>${postgres.version}</version>
    <scope>compile</scope>
  </dependency>
  -->

	    <dependency>
	        <groupId>com.ibm.db2</groupId>
	        <artifactId>db2-jdbc</artifactId>
	        <version>${db2.version}</version>
	        <scope>compile</scope>
	    </dependency>
 
		<!--
        <dependency>
            <groupId>com.microsoft.sqlserver</groupId>
            <artifactId>sqljdbc4</artifactId>
            <version>${mssql.version}</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>${mysql.version}</version>
        </dependency>
        <dependency>
            <groupId>com.oracle</groupId>
            <artifactId>ojdbc6_g</artifactId>
            <version>${oracle.version}</version>
        </dependency>
        <dependency>
            <groupId>org.sybase</groupId>
            <artifactId>sybase-jconnect</artifactId>
            <version>${sybase.version}</version>
        </dependency>
	    -->
    </dependencies>
```

## Step 4: Test the Configuration

Running the `dbtest` ant target will tell you if you have configured the database connection properly.

```shell
ant dbtest
```

## Step 5: Build and Deploy 

Following a successful test, you can execute the command below to build the database tables and copy files to your servlet container. 

Executing the command `ant clean initportal` **will drop and recreate the database tables and all existing data will be lost**. This will result in a clean uPortal database structure. If you want to keep the contents of your existing database, use `ant clean deploy-war` .

```shell
ant clean initportal
```

## Step 6: Restart Tomcat


 

##  Issues and Known Bugs

Some people have encountered problems with database drivers with certain web application environments if the classes zip file is used as-is with the `.zip` file extension. Simply renaming the file to a `.jar` file seems to fix the problem. Alternatively, unzipping the classes file into a directory structure, then using the jar command to repackage the classes into a jar file works as well.
