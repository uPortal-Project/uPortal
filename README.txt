$Id$

uPortal 2.4.2 Distribution
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

Please report bugs and suggestions:

 http://www.uportal.org/bugzilla/

uPortal website:

 http://www.uportal.org


Release Notes - uPortal - Version 2.4.2
---------------------------------------
** Bug
    * [UP-338] - RENDERING_DONE Event never sent
    * [UP-476] - User's LDAP groups not loaded if username contains uppercase
    * [UP-744] - PersonDirectory has a memory leak related to caching IPersons in a WeakHashMap
    * [UP-745] - ChannelManager has a memory leak, when it swaps out a channel for the CError channel, the end session events never progagate to the original channel
    * [UP-746] - CSecureInfo has a memory leak, when ChannelManager swaps out a channel for the CSecureInfo channel, the to end session events never propagate to the original channel
    * [UP-747] - Change to portlet parameter encoding breaks download worker URLs
    * [UP-748] - Infinite recursion in RestrictedPerson
    * [UP-749] - render parameter does not survive refresh
    * [UP-753] - ChannelFactory should not expose internal map of static channels, not create more than one instance of a multithreaded channel
    * [UP-759] - Xalan jar should be deployed to endorsed directory
    * [UP-760] - Entity locks not expired correctly
    * [UP-761] - A lock owner is limited to single READ lock on an entity
    * [UP-772] - Classpath resources not being copied to build
    * [UP-775] - Duplicate read locks for a single owner not permitted.
    * [UP-776] - WebApplicationMarshaller.java turns resource-ref into resource-env-ref
    * [UP-778] - AggregatedLayoutManager.loadUserLayout() fails to log stack trace for exception
    * [UP-779] - ChannelRenderer declares constants that are already declared in its base class
    * [UP-780] - Eliminate "unknown additional descriptor warning" when using ChainingSecurityContext
    * [UP-796] - contains() doesn't always work for PAGS groups
    * [UP-798] - GroupService.isComposite always returns null


** Improvement
    * [UP-770] - Document hsqldb version included with uPortal
