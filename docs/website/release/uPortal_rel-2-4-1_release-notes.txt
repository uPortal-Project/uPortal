uPortal 2.4.1 Distribution
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
1) uPortal 2.4.1
2) uPortal 2.4.1 java libraries (Xalan, Pluto, etc).
3) uPortal Architecture overview and JavaDoc API
   (see docs directory)


System requirements
-------------------
JDK 1.3 or higher
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


Changes since uPortal 2.4
-------------------------

************
* Database *
************

-Bug 1710, Changed not-null constraint of UP_PORTLET_ENTITY_PREFS and UP_PORTLET_DEFINITION_PREFS
           from PORTLET_PREFERENCES_NAME (which didn't exist) to PORTLET_PREF_NAME.


**************
* Properties *
**************

-security.properties
  
   Changed naming convention of security context property parameters.


*************
* Bug fixes *
*************

-Bug 1504, Supported portlet ResourceBundles and made portlet-info optional.
-Bug 1550, Removed duplicate setting the uploaded file in the parameter Map.
-Bug 1633, Made portlets work after web application is reloaded.
-Bug 1660, Prevented setting of invalid channel timeouts.
-Bug 1691, Set runtime data on error channel.
-Bug 1693, Made sure channel registry in CContentSubscriber is properly updated.
-Bug 1695, Fixed ExternalServices parser error.
-Bug 1700, Fixed WSRP Consumer NullPointerException caused by missing user attribute.
-Bug 1702, Fixed logging calls to use log.error(String, Throwable) rather than log.error(Object).
-Bug 1703, Replaced Campus Pipeline license with JA-SIG license in test source files.
-Bug 1704, Set new LocaleManager when UserProfile is created in RDBMUserLayoutStore.getProfileById().
-Bug 1706, Changed regular statements to prepared statements in RDBMUserLayoutStore.setUserLayout() method.
-Bug 1710, (See database section above).
-Bug 1711, Prevented anchors from being added to javascript commands.
-Bug 1713, Fixed label of portlet management interface parameter.
-Bug 1715, Fixed improper handling of channel parameter override attribute.
-Bug 1716, Saved layout in CFragmentManager to prevent NPE.
-Bug 1728, Updated cache of persons when a user succesfully authenticates.
-Bug 1729, Fixed classpath for pubchan ant target so that log messages are handled properly.
-Bug 1737, Replaced ldap.properties default connection with ldap.xml default connection when specified.
-Bug 1741, Fixed ability to recover from missing profile in AggregatedUserLayoutStore.
-Bug 1744, Fixed portlet file uploading ability.
-Bug 1750, ExceptionHelper.shortStackTrace() no longer obscures meaningful cause of exception.
-Bug 1756, Fixed logic in LRUCache to avoid endless sweep.
-Bug 1757, Restored upgrade and locale features missing from the DbLoader in uPortal 2.4.
-Bug 1764, Changed DbUtils to use first data type mapping when more than one is available.
-Bug 1774, Enabled chained security contexts to be more than 2 levels deep.
-Bug 1775, Changed PersonDirectory to assume that a base DN from ldap.xml includes the usercontext.


*****************
* Other changes *
*****************

-Updated Pluto and testsuite portlet to CVS snapshot on November 2, 2004.
-Updated WSRP4J to CVS snapshot on November 2, 2004.