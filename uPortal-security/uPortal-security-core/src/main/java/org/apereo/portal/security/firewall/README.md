RequestParameterPolicyEnforcementFilter
=======================================

The `RequestParameterPolicyEnforcementFilter` is a blunt generic Java Servlet Filter suitable for 

 * in general, blocking malicious requests involving unexpectedly duplicate request parameters or request parameters with unexpected and poorly handled characters in their values.
 * in specific, blocking the malicious requests involved in `CVE-2014-4172`, to which certain Java CAS Client versions 
are vulnerable, including Java CAS Client versions that have been included in uPortal releases.

It is a fork of that offered to the `cas-server-security-filter` project at https://github.com/Jasig/cas-server-security-filter/pull/6 .

It is optionally configurable as to what parameters it checks, what characters it forbids in those checked parameters, 
and whether it allows those checked parameters to be multi-valued.

It has no dependencies.

Configuration warning : Filter order matters
--------------------------------------------

The `RequestParameterPolicyEnforcementFilter` MUST be mapped BEFORE the `CAS Validate Filter` so that it is filtering
 the inputs to the Java CAS Client Filter.  So, take great care with your `web.xml` `filter-mapping` order.

Configuration options
---------------------

This Filter is optionally configured via Filter `init-param` in `web.xml`.

In general the Filter is very persnickety about init-params, such that if you give it a configuration that the Filter
 is not totally sure it understands, it will fail Filter initialization.

### parametersToCheck init-param


The _optional_ init-param `parametersToCheck` is a whitespace-delimited set of the names of request parameters the 
Filter will check.

The special value `*` instructs the Filter to check all parameters, and is the default behavior.

### charactersToForbid init-param

The _optional_ init-param `charactersToForbid` is a whitespace-delimited set of the individual characters the Filter 
will forbid.

The special magic value of exactly `none` instructs the Filter not to forbid any characters. (This is useful for 
using the Filter to block multi-valued-ness of parameters without sniffing on any characters.)

If not present, the Filter will default to forbidding the characters `? # & %` .

### allowMultiValuedParameters init-param

The _optional_ init-param `allowMultiValuedParameters` indicates whether the Filter should allow multi-valued 
parameters.

If present the value of this parameter must be exactly `true` or `false`, with `false` as the default.



Configuration Examples
----------------------

### The recommended and included configuration

This filter is wired up in the included `web.xml` to block illicit characters in values of and multiple values of the
 request parameters involved in using the Java CAS Client, since these characters should not be in CAS ticket 
 identifiers and there is no legitimate use case for submission of CAS protocol parameters as multi-valued.
 
     <filter>
       <filter-name>requestParameterFilter</filter-name>
       <filter-class>org.apereo.portal.security.firewall.RequestParameterPolicyEnforcementFilter</filter-class>
      <init-param>
        <param-name>parametersToCheck</param-name>
        <param-value>ticket SAMLArt pgtIou pgtId</param-value>
      </init-param>
     </filter>
     
     ...

    <filter-mapping>
        <filter-name>requestParameterFilter</filter-name>
        <url-pattern>/Login</url-pattern>
        <url-pattern>/CasProxyServlet</url-pattern>
    </filter-mapping>


### The no-configuration behavior

In principle, all `init-param`s configuring the RequestParameterPolicyEnforcementFilter are optional and it can operate with default configuration.

    <filter>
      <filter-name>requestParameterFilter</filter-name>
      <filter-class>org.apereo.portal.security.firewall.RequestParameterPolicyEnforcementFilter</filter-class>
    </filter>
    ...
    <filter-mapping>
      <filter-name>requestParameterFilter</filter-name>
      <url-pattern>/*</url-pattern>
    </filter-mapping>

In this configuration, the Filter will scrutinize all request parameters, requiring that they not be multi-valued, and requiring that they not contain any of `% ? # &`.

This configuration is not appropriate in uPortal, where multi-valued parameters and parameters containing interesting characters are valid for some request parameters in some circumstances.  That is, the default configuration is over-broad in what it blocks.

### Allow multi-valued parameters

Multi-valued parameters are essential for supporting forms with multi-choice selectors where the form submission is 
legitimately represented as repeated parameter names with different values.

So, if you want to scrutinize the characters in all parameters, you might have to relax the requirement that those 
parameters not be multi-valued.

    <filter>
      <filter-name>requestParameterFilter</filter-name>
      <filter-class>org.apereo.portal.security.firewall.RequestParameterPolicyEnforcementFilter</filter-class>
      <init-param>
        <param-name>allowMultiValuedParameters</param-name>
        <param-value>true</param-value>
      </init-param>
    </filter>
    ...
    <filter-mapping>
      <filter-name>requestParameterFilter</filter-name>
      <url-pattern>/*</url-pattern>
    </filter-mapping>
    
This configuration isn't necessary in the included `web.xml` configuration because the CAS protocol parameters do not need to be multi-valued.


### Restrictions suitable for fronting a CAS Server

Likewise, you could use this Filter in front of a CAS Server to prevent unexpected multi-valued submissions of CAS 
protocol parameters.

    <filter>
      <filter-name>requestParameterFilter</filter-name>
      <filter-class>org.apereo.portal.security.firewall.RequestParameterPolicyEnforcementFilter</filter-class>
      <init-param>
        <param-name>parametersToCheck</param-name>
        <param-value>ticket SAMLArt service renew gateway warn logoutUrl pgtUrl</param-value>
      </init-param>
      <init-param>
        <param-name>charactersToForbid</param-name>
        <param-value>none</param-value>
      </init-param>
    </filter>
    ...
    <filter-mapping>
      <filter-name>requestParameterFilter</filter-name>
      <url-pattern>/*</url-pattern>
    </filter-mapping>
    
This approach has the advantage of only blocking specific CAS protocol parameters, 
so that if you were to map the Filter in front of say the services management UI you can block unexpectedly 
multi-valued CAS protocol parameters without blocking submission of the services management edit screen where 
multiple user attributes are selected for release to a service (a legitimate case of a multi-valued attribute).

### An entirely novel configuration

So, a neat thing about this Filter is that it has nothing to do with CAS and it has no dependencies at all other than the Servlet API, so on that fateful day when you discover that some Java web application has some problem involving illicit submissions of the semicolon character in a request parameter named `query`, you can plop this Filter in front of it and get back to safety.  Doing so will almost certainly just work, since this Filter has no external dependencies whatsoever except on the Servlet API that had to be present for that Web Application to be a Java web app anyway.
 

     <filter>
       <filter-name>requestParameterFilter</filter-name>
       <filter-class>org.apereo.portal.security.firewall.RequestParameterPolicyEnforcementFilter</filter-class>
       <init-param>
         <param-name>parametersToCheck</param-name>
         <param-value>query</param-value>
       </init-param>
       <init-param>
         <param-name>charactersToForbid</param-name>
         <param-value>;</param-value>
       </init-param>
     </filter>
     ...
     <filter-mapping>
       <filter-name>requestParameterFilter</filter-name>
       <url-pattern>/*</url-pattern>
     </filter-mapping>
