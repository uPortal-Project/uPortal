
HOW TO CONFIGURE AGGREGATED LAYOUTS:




1) To register AL theme/structure stylesheets and also
to assign them as default system stylesheets in the
user profile the ant target "alinstall" that is defined 
in target.xml must be executed:

 ant alinstall -Daction=add

To get back to the previous stylesheets and the user
profile the following command is used:

 ant alinstall -Daction=delete

In both cases Ant executes the Java class 
org.jasig.portal.layout.utils.ALMigrationUtil

2) When switching to AL in the portal.properties file
the user layout manager and the layout store
implementation need to be changed to the versions that
support AL:

org.jasig.portal.UserLayoutStoreFactory.implementation
= org.jasig.portal.layout.AggregatedUserLayoutStore

org.jasig.portal.layout.UserLayoutManagerFactory.coreImplementation
= org.jasig.portal.layout.AggregatedUserLayoutImpl

3) restart the tomcat and get the new uPortal UI with
integrated modes.