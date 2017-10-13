# uPortal Authentication
uPortal supports various methods to authenticate users.

+  Internal Authentication (TBD)
+ [Redirect Guest to CAS Sign-In](redirect-guest-to-cas.md)
+ [CAS 5 ClearPass: Credential Caching and Replay](Cas5ClearPass.md)

## Redirecting Guests to CAS for Sign-In

An occasional feature request is for uPortal to only support authenticated users,
skipping a guest experience all together.

To enable the redirect, set the following property to true in `${portal.home}/uPortal.properties`:

```properties
cas.enable.redirect.guest.to.login=true
```
