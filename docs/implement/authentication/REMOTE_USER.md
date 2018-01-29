# `REMOTE_USER` Authentication

uPortal can authenticate users based on the value of `HttpServletRequest.getRemoteUser()`, which
will be non-null if the user has logged in _via_ some form of container-managed authentication.
uPortal can support a wide variety of authentication providers in this way, both commercial and open
source.  Some examples are [Shibboleth][]/SAML (using `mod_shib` in Apache HTTPD) and [CoSign][]
(using `mod_cosign` in Apache HTTPD).

## Enabling `RemoteUserSecurityContextFactory`

To permit `REMOTE_USER` authentication in uPortal, add the following property to
`uPortal.properties`:

```properties
org.apereo.portal.security.provider.RemoteUserSecurityContextFactory.enabled=true
```

This setting will instruct uPortal to honor `HttpServletRequest.getRemoteUser()`;  you will also
need to implement some form of integration component in the container (depending on authentication
provider) to make this value present in the request.

## Note on `RemoteUserPersonManager`

:notebook: Beginning with uPortal 5.0.5, explicitly configuring `RemoteUserPersonManager` in Spring
is not required.  uPortal will automatically apply the behavior of `RemoteUserPersonManager`
whenever `RemoteUserSecurityContextFactory` is enabled.

[Shibboleth]:https://www.shibboleth.net/
[CoSign]:http://weblogin.org/
