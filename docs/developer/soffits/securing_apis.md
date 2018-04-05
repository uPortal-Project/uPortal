# Securing APIs

Soffit projects often provide HTTP-based APIs for supplying data to UI components or performing
operations on persistent data stores.  In fact, this practice is preferred and recommended over
_Server-Side Rendering_ strategies that generate dynamic HTML with data already baked-in.  Modules
that provide separate "front-end" (UI) and "back-end" (server-side) logic are easier to understand,
maintain, and extend.

In most cases these module-provided APIs follow the principals of <i>Representational State
Transfer</i> (REST) and use JSON as a data exchange format.  Both of these practices are also
preferred and recommended.

Often these APIs require some information from a trusted source.  For example, they may need to know
the identity and/or characteristics of the user, may provide access to sensitive data, or both.

uPortal provides a simple, secure way of sharing this information with module-provided APIs (_i.e._
APIs within uPortal modules).  This capability is based on the
`SoffitApiPreAuthenticatedProcessingFilter` and [Spring Security][].  The Java class files for this
feature are available in the `uPortal-soffit-renderer` dependency.

:notebook:  A module need not be a Soffit to use these features, but it really _should_ be a module.
In other words, APIs that are secured using this approach should have providing data and services
_within the portal_ as their original and essential purpose.  The reason for this distinction is
that the information this approach provides is proxy of the user's profile _within the portal_, and
that may not be (often isn't) the same thing as the user's identity within the organization.

## Example Implementation

This capability is based on the `SoffitApiPreAuthenticatedProcessingFilter` and [Spring Security][].
The Java class files for this feature are available in the `uPortal-soffit-renderer` dependency.

The `SoffitApiPreAuthenticatedProcessingFilter` relies on a `Bearer` token in the `Authorization`
HTTP header.  You can obtain an appropriate token from uPortal's `userinfo` endpoint, which is found
at the following URI: `/uPortal/api/v5-1/userinfo`.  Any portal user (including unauthenticated
users) may obtain a personal token from this endpoint.  This token should be obtained with
JavaScript and passed (also with JavaScript) as a `Bearer` token in the `Authorization` header in
API requests.

Refer to the sections below to secure API endpoints and access information about the portal user.

### `uPortal-soffit-renderer` Dependency (Gradle Example)

```groovy
compile "org.jasig.portal:uPortal-soffit-renderer:${uPortalVersion}"
```

### Spring Security Configuration

This example defines the following rules for accessing API enpoints that begin with `/api`:

  - Authenticated users may send `GET` and `POST` requests
  - No users may send any other type of request

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilter(preAuthenticatedProcessingFilter())
            .authorizeRequests()
                .antMatchers(HttpMethod.GET,"/api/**").authenticated()
                .antMatchers(HttpMethod.POST,"/api/**").authenticated()
                .antMatchers(HttpMethod.DELETE,"/api/**").denyAll()
                .antMatchers(HttpMethod.PUT,"/api/**").denyAll()
                .anyRequest().permitAll();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new SoffitApiAuthenticationManager();
    }

    @Bean
    public AbstractPreAuthenticatedProcessingFilter preAuthenticatedProcessingFilter() {
        final AbstractPreAuthenticatedProcessingFilter rslt = new SoffitApiPreAuthenticatedProcessingFilter();
        rslt.setAuthenticationManager(authenticationManager());
        return rslt;
    }

}
```

### Accessing User Information within the API

Information about the user can be obtained from the Spring `SecurityContextHolder`:

```java
final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
final String username = (String) authentication.getPrincipal();
final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
```

[Spring Security]: https://projects.spring.io/spring-security/
