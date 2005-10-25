$Id$

uPortal Quick-Start Distribution
--------------------------------------

Purpose
-------
This quick-start release distribution is provided
for those who want to get familiar with uPortal 
quickly and easily, without having to compile and
deploy code, and without having to set up a database.


Contents
--------
1) uPortal 2.4.2
2) Tomcat 5.0.28
3) HSQLDB 1.7.2.4
4) Ant 1.6.2


System requirements
-------------------
JDK 1.4 or higher.
JDK must include tools.jar.
JAVA_HOME environment variable must be set to a path that does not contain spaces.
Nothing else should be running on port 8005, 8080 and 8887.
Path to uPortal_2-4-quick-start folder should not contain spaces.


Instructions
------------
For the commands below, the 'ant' command depends on
your operating system:
Windows: ant.bat
UNIX:    ant.sh

To start uPortal, you must first start HSQLDB by typing:

ant hsql

Next start Tomcat by typing:

ant tomcatstart

You may need to enter each command
in a separate console window.  

Once HSQLDB and Tomcat are running, you can access
uPortal with your browser by entering the following URL:

http://localhost:8080/uPortal/

You can login to uPortal with the following user name/password 
combinations:

demo, demo
student, student
faculty, faculty
staff, staff
developer, developer

To stop uPortal, first stop Tomcat by typing

ant tomcatstop

Then stop Hypersonic SQL by
typing

CTRL-C

in the window in which it was started.


Notes 
-----
-uPortal binaries are included so there is no
 need to compile any uPortal source code.  Also, the
 database is pre-loaded, so there is no need to run
 any database scripts.  A separate build.xml file
 exists in the uPortal sub-directory which can be
 used to deploy uPortal if you make any changes to
 its source code or properties files.  The included 
 binaries where compiled with JDK 1.4.2-b28.

-Connection pooling is set up by Tomcat.
 For more information about Tomcat's conneciton
 pooling configuration, see 
 http://jakarta.apache.org/tomcat/tomcat-5.0-doc/jndi-datasource-examples-howto.html

-Log messages for uPortal appear in
 portal.log which appears in the current java working 
 directory which is typically the same directory that 
 this README.txt file is in.

-If you are attempting to run uPortal in an environment
 that requires a proxy server to make http requests outside
 the firewall, you may need to configure the proxy host
 and port for your JVM.  This can be done by modifying the
 build.xml file by adding the following lines in the
 tomcatstart target element below the catalina.home
 sysproperty element:
 <sysproperty key="http.proxyHost" value="YOUR_PROXY_HOST"/>
 <sysproperty key="http.proxyPort" value="YOUR_PROXY_PORT"/> 


Please report bugs and suggestions:

 http://www.uportal.org/bugzilla/

uPortal website:

 http://www.uportal.org/


Changes since last release
-------------------------
See ./uPortal_rel-X-X-X/README.txt

This quick-start distribution now contains jta.jar
so that certain tools will run without an available
JNDI context.
