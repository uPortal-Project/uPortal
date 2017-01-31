# Use JNDI to override values for some properties

## Where applicable

It is quite easy to set override values properties in

+ `rdbm.properties`
+ `security.properties`
+ `portal.properties`

These files are loading into the Spring context via
`PortalPropertySourcesPlaceholderConfigurer`. This class also loads `JNDI`
values which supersede the values in the configured properties files.

## Example

The property `org.apereo.portal.channels.CLogin.CasLoginUrl` is defined in
`security.properties`. This value is usually built up from
values in the filters file used to build `uportal.war`. This value usually
differs between environments.

### Define the value

First, we need to define the value. Using Tomcat, place the following
line in `conf/server.xml` inside `<GlobalNamingResources>`:

```xml
<Environment 
  name="cas/login"
  value="http://localhost:8903/cas/login?service=..."
  type="java.lang.String" />
```

Here, we are defining "cas/login" with a value for the URL in Tomcat.

### Tell uPortal to use the value

We need to tell uPortal we want to use this global value. In
`uportal-war/src/main/webapp/META-INF/context.xml`, we add a resource link
and provide our local name inside `<Context>`:

```xml
<ResourceLink 
  name="org.apereo.portal.channels.CLogin.CasLoginUrl"
  global="cas/login" 
  type="java.lang.String" />
```

We are using the short global name and assigning it to the longer
properties key for uPortal.

### Reference the resource from uPortal's `web.xml`

The third (and last!) change is to add the reference to
`uportal-war/src/main/webapp/WEB-INF/web.xml`:

```xml
<resource-env-ref>
  <resource-env-ref-name>org.apereo.portal.channels.CLogin.CasLoginUrl</resource-env-ref-name>
  <resource-env-ref-type>java.lang.String</resource-env-ref-type>
</resource-env-ref>
```

That's it!! (... After some testing, this last step looks unnecessary).

### Deploy the change

WARNING: `ant clean` is potentially destructive. Know what you are doing.

Perform an `ant clean deploy-war` and restart Tomcat (to pick up the
JNDI entry). The value defined in JNDI should now be in use by uPortal.



Source: [uportal-user@ thread](https://groups.google.com/a/apereo.org/d/topic/uportal-user/IM0SnpIlJC0/discussion).
