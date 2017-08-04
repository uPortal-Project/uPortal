# Publishing a Soffit

Follow these steps to view your soffit in uPortal.

## In the Portlet Manager

1. Select _Register New Portlet_
2. Choose _Portlet_ in the list of types and click _Continue_
3. Select _/uPortal_ and _Soffit Connector_ in the Summary Information screen
   and click _Continue_
4. Enter portlet metadata normally (_e.g._ name, tile, fname, groups,
   categories, lifecycle state, _etc._)
5. Under Portlet Preferences, override the value of
   `org.apereo.portal.soffit.connector.SoffitConnectorController.serviceUrl`
   with the URL of your soffit, _e.g._ `http://localhost:8090/soffit/my-soffit`
   running independently (outside Tomcat) or
   `http://localhost:8080/my-porject/soffit/my-soffit` running inside Tomcat
6. Click _Save_

After completing these steps, you should be able to find your soffit using the
Search interface or add it to your layout with the Personalization Gallery.
