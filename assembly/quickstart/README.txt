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
- On windows an unzip utility OTHER than the Extrator utility that comes with
  the OS is required. See: http://www.ja-sig.org/issues/browse/UP-2024


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
 deploy changes made to the included uPortal source to the quickstart tomcat
 instance. To run Ant targets for the included uPortal source code cd into the
 @uportal.name@ directory. Running the following command from the @uportal.name@
 directory will list available uPortal Ant targets:

../ant -p

-Log messages for uPortal appear in @tomcat.name@/portal.log. Log files for
 Tomcat appear in @tomcat.name@/logs
 

Developing with the Quickstart
--------------------------------------------------------------------------------
The developers build of the quickstart (distinguished by a -dev suffix in the
download name) includes a copy of uPortal that is still 'attached' to subversion.
Running svn commands in the @uportal.name@ directory will work as if you checked
out uPortal from the JA-SIG Subversion repository.




Contact
--------------------------------------------------------------------------------
Please report bugs and suggestions:

 http://www.ja-sig.org/issues/browse/UP

uPortal website:

 http://www.uportal.org/


