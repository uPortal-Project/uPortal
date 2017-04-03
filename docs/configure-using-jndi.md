# JNDI Usage in uPortal

JNDI can be used to populate values in various areas of uPortal configuration.

- Override properties values in Spring
- Define uPortal DataSources
- Configure CAS Filters

## Use JNDI to override values for some properties in Spring

### Where applicable

It is quite easy to set override values properties in

+ `rdbm.properties`
+ `security.properties`
+ `portal.properties`

These files are loading into the Spring context via
`PortalPropertySourcesPlaceholderConfigurer`. This class also loads `JNDI`
values which supersede the values in the configured properties files.

### Example

The property `org.apereo.portal.channels.CLogin.CasLoginUrl` is defined in
`security.properties`. This value is usually built up from
values in the filters file used to build `uportal.war`. This value usually
differs between environments.

#### Define the value

First, we need to define the value. Using Tomcat, place the following
line in `conf/server.xml` inside `<GlobalNamingResources>`:

```xml
<Environment 
  name="cas/login"
  value="http://localhost:8903/cas/login?service=..."
  type="java.lang.String" />
```

Here, we are defining "cas/login" with a value for the URL in Tomcat.

#### Tell uPortal to use the value

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

#### Reference the resource from uPortal's `web.xml`

The third (and last!) change is to add the reference to
`uportal-war/src/main/webapp/WEB-INF/web.xml`:

```xml
<resource-env-ref>
  <resource-env-ref-name>org.apereo.portal.channels.CLogin.CasLoginUrl</resource-env-ref-name>
  <resource-env-ref-type>java.lang.String</resource-env-ref-type>
</resource-env-ref>
```

That's it!! (... After some testing, this last step looks unnecessary).

#### Deploy the change

WARNING: `ant clean` is potentially destructive. Know what you are doing.

Perform an `ant clean deploy-war` and restart Tomcat (to pick up the
JNDI entry). The value defined in JNDI should now be in use by uPortal.

## Define uPortal DataSources

Defining DataSources external to an application was one of the earliest
uses of JNDI. uPortal has support for JNDI DataSources. In addition,
a helper factory bean can be configured to use the first working bean.
 This allows the definition of both a JNDI DataSource bean along with
 a fallback bean that is defined with filter file values for testing.
 
Example from datasourceContext.xml:

```xml

    <!--
    <bean id="PortalDb" parent="basePooledDataSource">
        <qualifier value="PortalDb"/>
        <property name="driverClassName" value="${hibernate.connection.driver_class}" />
        <property name="url" value="${hibernate.connection.url}" />
        <property name="username" value="${hibernate.connection.username}" />
        <property name="password" value="${hibernate.connection.password}" />
    </bean>
    -->
    <bean id="PortalDb" class="org.apereo.portal.spring.beans.factory.MediatingFactoryBean">
        <property name="type" value="javax.sql.DataSource" />
        <property name="delegateBeanNames">
            <list>
                <value>PortalDb.JNDI</value>
                <value>PortalDb.direct</value>
            </list>
        </property>
    </bean>

    <bean id="PortalDb.JNDI" class="org.springframework.jndi.JndiObjectFactoryBean">
        <property name="jndiName" value="java:comp/env/jdbc/PortalDb" />
    </bean>

    <bean id="PortalDb.direct" class="org.apache.commons.dbcp.BasicDataSource" lazy-init="true">
        <property name="driverClassName" value="${hibernate.connection.driver_class}" />
        <property name="url" value="${hibernate.connection.url}" />
        <property name="username" value="${hibernate.connection.username}" />
        <property name="password" value="${hibernate.connection.password}" />

        <property name="maxActive" value="50" />
        <property name="maxIdle" value="10" />
        <property name="maxWait" value="1000" />
        <property name="removeAbandoned" value="true" />
        <property name="removeAbandonedTimeout" value="300" />
        <property name="logAbandoned" value="true" />
    </bean>

    <bean id="PortalDB.metadata" class="org.apereo.portal.jdbc.DatabaseMetaDataImpl">
        <constructor-arg index="0" ref="PortalDb" />
        <constructor-arg index="1" ref="transactionManager"/>
    </bean>

```

## Configure CAS Filters

The CAS Client is JNDI-aware. JNDI values supersede those defined in `web.xml`.
Again, the JNDI names are mapped to global names in `uportal-war/src/main/webapp/META-INF/context.xml`.
The global names (and actual values) are defined in Tomcat.

```xml
    <!-- required names for CAS client -->
    <ResourceLink name="cas/casServerUrlPrefix" global="cas/casServerUrlPrefix" type="java.lang.String" />
    <ResourceLink name="cas/service" global="uportal/service" type="java.lang.String" />
    <!-- Conflicts with cas/service, which is needed for Authentication Filter
    <ResourceLink name="cas/serverName" global="uportal/casServerName" type="java.lang.String" />
    -->
    <ResourceLink name="cas/proxyCallbackUrl" global="shared/url/my" type="java.lang.String" />
    <ResourceLink name="cas/casServerLoginUrl" global="cas/casServerLoginUrl" type="java.lang.String" />
```

As noted in the example above, take care with conflicting configuration. Each CAS filter will read all the CAS values defined.

Source: [uportal-user@ thread](https://groups.google.com/a/apereo.org/d/topic/uportal-user/IM0SnpIlJC0/discussion).
