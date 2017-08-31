# Redirecting Guests to CAS for Sign-In

An occasionally feature request is for uPortal to only support authenticated users,
skipping a guest experience all together.

This can be accomplished by wrapping the CAS Authentication Filter around `/Login`.

Add the CAS Authentication Filter to `uportal-war/src/main/webapp/WEB-INF/web.xml`:

```xml
    <filter>
        <filter-name>CAS Authentication Filter</filter-name>
        <filter-class>org.jasig.cas.client.authentication.AuthenticationFilter</filter-class>
        <init-param>
            <param-name>casServerLoginUrl</param-name>
            <param-value>${environment.build.cas.protocol}://${environment.build.cas.server}${environment.build.cas.context}/login</param-value>
        </init-param>
        <init-param>
            <param-name>service</param-name>
            <param-value>${environment.build.uportal.protocol}://${environment.build.uportal.server}${environment.build.uportal.context}/Login</param-value>
        </init-param>
        <init-param>
            <param-name>gateway</param-name>
            <param-value>false</param-value>
        </init-param>
        <!-- optional -->
        <init-param>
            <param-name>encodeServiceUrl</param-name>
            <param-value>true</param-value>
        </init-param>
    </filter>
```


Then, apply the filter to `/login` as the first `filter-mapping`:

```xml
    <!-- FILTER ORDER IS VERY IMPORTANT. DO NOT CHANGE ORDER WITHOUT VERY GOOD REASON -->

    <filter-mapping>
        <filter-name>CAS Authentication Filter</filter-name>
        <url-pattern>/Login</url-pattern>
    </filter-mapping>
```

## Using JNDI

To add support for JNDI, simply map CAS predefined variables names to the global JNDI names
in Tomcat. The CAS names are the parameter names prefixed with "cas/", for example "cas/service" value
will be used by CAS filters as the value for the `service` init-param.
This is done in `uportal-war/src/main/webapp/META-INF/context.xml`:

```xml
    <ResourceLink name="cas/casServerLoginUrl" global="cas/casServerLoginUrl" type="java.lang.String" />
    <ResourceLink name="cas/service" global="uportal/service" type="java.lang.String" />
```

The Global names are arbitrary while the resource names are not.

If JNDI values are present, they will supersede these defined in `web.xml`.
For good measure, comment out the init-params you define with JNDI to confirm the 
values in `web.xml` are not in use.

See [Configuring using JNDI](configure-using-jndi.md)