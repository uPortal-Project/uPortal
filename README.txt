$Id$

uPortal Distribution
--------------------------

Purpose
-------
This distribution is targeted towards
people who would like to install
uPortal in a development or production 
environment.  It contains uPortal source
code and properties files, but not a servlet
container, directory, or relational database, 
which are required.  Use of this distribution
requires knowledge of J2EE environments and the
Ant tool from the Apache Jakarta project.


Contents
--------
1) uPortal
2) uPortal dependencies (Xalan, Pluto, etc).
3) uPortal Architecture overview and JavaDoc API


System requirements
-------------------
JDK 1.4 or higher
JAVA_HOME environment variable must be set
Installation of Ant from Jakarta: 
  http://ant.apache.org/


Instructions
------------
Modify build.properties with local settings.

Use the following ant targets:

initportal - installs uPortal into a servlet container, 
             prepares a relational database with uPortal schema and data,
	     publishes channels and layout fragments,
             deploys portlet applications
dist - creates JavaDoc, uPortal jar, and uPortal WAR files.

For descriptions of other Ant targets, 
see docs/uPortal_tools_overview.txt.

Make sure the database and servlet container are running and
access uPortal with http://localhost:8080/uPortal/
Your URL may be different if you have configured a different port
and/or context name.

You can login to uPortal with the following user name/password 
combinations:

demo, demo
student, student
faculty, faculty
staff, staff
developer, developer

Each user may have a slightly different layout.


Notes 
-----
-Several properties files and one database table changed
 since uPortal 2.3.x.  If you are upgrading from this version,
 you will need to see to it that your properties files and
 database data are upgraded appropriately.  The difficulty
 of upgrading from 2.3.x to 2.4.x is expected to be much lower 
 than similar upgrades in past versions.

-uPortal does not strictly require the use of connection
 pooling, but it is highly recommended for production
 installations.  If your servlet container does not provide
 connection pooling, try something like Yet Another Poolman.  
 For information about Yet Another Poolman, 
 see http://yapoolman.sourceforge.net/.

-Bug fixes from 2.3.x releases have been implemented in 2.4.x
 whenever they were appropriate

-If you experience XML/XSLT-related errors, there is a good
 chance that your environment is not using the version of
 Xalan/Xerces that comes with this release. If you are using
 Tomcat, try copying the following files into Tomcat's
 common/endorsed directory: xalan.jar, xercesImpl.jar, xml-apis.jar.
 Alternatively, you can specify the use of these jar files
 via a java parameter when starting your servlet container:
  -Xbootclasspath/p:xalan.jar;xercesImpl.jar;xml-apis.jar
 For more information, see Version section of
 http://xml.apache.org/xalan-j/faq.html

-JSR 168 Portlets are supported in this version of uPortal.
 For information on installing Portlets, see 
 http://www.uportal.org/implementors/portlets/workingWithPortlets.html.

-WSRP functionality was included in uPortal as early as uPortal 2.2.
 However, beginning with uPortal 2.4, the WSRP consumer has been replaced with
 one based on WSRP4J.  The WSRP producer has been deprecated but may
 be reintroduced in a future release.
 
-The WSRP consumer proxyportlet requires additional .jar files to operate under 
JDK 1.5.  Specifically, under JDK 1.5 you'll need to install xml-apis.jar, 
xmlParserAPIs.jar, and xercesImpl.jar into proxyportlet/WEB-INF/lib, as described
at 
http://www.ja-sig.org/wiki/display/UPC/_WSRP+Consumer+Support+for+uPortal+2.5+and+uPortal+2.4+on+JDK+1.5
( https://www.ja-sig.org/wiki/x/OzM )

Please report bugs and suggestions:

 http://www.uportal.org/bugzilla/

uPortal website:

 http://www.uportal.org
