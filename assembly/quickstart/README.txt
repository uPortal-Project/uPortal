uPortal Quick-Start Distribution
--------------------------------------------------------------------------------

Purpose
--------------------------------------------------------------------------------
This quick-start release distribution is provided for those who want to get
familiar with uPortal quickly and easily, without having to compile and deploy
code, and without having to set up a database.


Contents
--------------------------------------------------------------------------------
1) uPortal @uportal.version@
2) Tomcat @tomcat.version@
3) Ant @ant.version@
4) Maven @maven.version@


System requirements
--------------------------------------------------------------------------------
- JDK 1.5 or later
- JDK must include tools.jar.
- JAVA_HOME environment variable must be set to a path that does not contain
  spaces.
- Nothing else should be running on ports 8005, 8080, or 8887.
- Path to the @quickstart.name@ folder should not contain spaces.


Instructions
--------------------------------------------------------------------------------
For the commands below, the 'ant' command depends on your operating system:
Windows: ant.bat
UNIX:    ant.sh

ant start
----------
Starts HSQL and then Tomcat, both are started in the background.

ant stop
----------
Stops Tomcat then stops HSQL. 


Using uPortal
--------------------------------------------------------------------------------
Once Tomcat is running you can access uPortal with your browser by entering:
http://localhost:8080/uPortal/

Logging in
----------
You can login to uPortal with the following user name, password combinations:

demo, demo
student, student
faculty, faculty
staff, staff
developer, developer


Notes
--------------------------------------------------------------------------------
-The uPortal source code is included with this distribution. A seperate
 build.xml exists in the @uportal.name@ directory that provides Ant targets to
 deploy changes to the quickstart tomcat instance. Running the following command
 will list available uPortal Ant targets:

ant -f @uportal.name@/build.xml -p

-Log messages for uPortal appear in @tomcat.name@/portal.log. Log files for
 Tomcat appear in @tomcat.name@/logs


Contact
--------------------------------------------------------------------------------
Please report bugs and suggestions:

 http://www.ja-sig.org/issues/browse/UP

uPortal website:

 http://www.uportal.org/


