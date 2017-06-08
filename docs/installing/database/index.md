# Configure the database

uPortal is configured to use a file-based HSQL database by default.

**This database configuration is not suitable for production deployments and best used for testing purposes.**

uPortal does support a number of popular production-class databases and you can configure the database by following the examples posted under Production Database Configuration.

## Step 1: Configure the Database Filter      

In the `filters` folder, locate the default `local.properties` file under `uPortal-4.1/filters/local.properties` and configure the Database Connection Settings

```shell
# HSQL Configuration
environment.build.hsql.port=8887

# Database Connection Settings (Uncomment the Maven Filters section in rdbm.properties)
environment.build.hibernate.connection.driver_class=org.hsqldb.jdbc.JDBCDriver
environment.build.hibernate.connection.url=jdbc:hsqldb:hsql://localhost:${environment.build.hsql.port}/uPortal
environment.build.hibernate.connection.username=sa
environment.build.hibernate.connection.password=
environment.build.hibernate.dialect=org.hibernate.dialect.HSQLDialect
environment.build.hibernate.connection.validationQuery=select 1 from INFORMATION_SCHEMA.SYSTEM_USERS
```

## Step 2: Add the database driver  

Open `uportal-db/pom.xml` file, uncomment and/or modify as needed the driver(s) of your choice.

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
	    <dependency>
	        <groupId>com.ibm.db2</groupId>
	        <artifactId>db2-jdbc</artifactId>
	        <version>${db2.version}</version>
	        <scope>compile</scope>
	    </dependency>
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

## Step 3: Test the database configuration

To test your database configuration from the command-line:

```shell_session
ant dbtest
```

## uPortal Production Database Configuration 

Select the database below for notes and examples of configuration.

+ [DB2](db2.md)
+ [Hypersonic](hypersonic.md)
+ [Microsoft SQL Server](ms-sqlserver.md)
+ [MySQL](mysql.md)
+ [Oracle RDBMS](oracle.md)
+ [PostgreSQL](postgresql.md)
+ [Sybase](sybase.md)
