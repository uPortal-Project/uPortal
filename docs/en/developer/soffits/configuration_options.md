# Configuration Options

## Caching

Caching soffit content in the portal is available _via_ the standard HTTP header
`Cache-Control`.  You must set `Cache-Control` as an HTTP response header to
take advantage of this feature.

### Example

``` http
Cache-Control: public, max-age=300
```

Cache scope may be `public` (shared by all users) or `private` (cached
per-user).  Specify `max-age` in seconds.

Cache re-validation is not yet supported, so...

``` http
Cache-Control: no-store
```

and...

``` http
Cache-Control: no-cache
```

currently have the same effect.
