# Security in uPortal

## Table of Contents

1. [CORS Filter](#cors-Filter)

## CORS Filter

The CORS Filter is an implementation of W3C's CORS 
(Cross-Origin Resource Sharing) specification, which
enables cross-origin requests.

### CORS Filter Configuration Options

<dl>
    <dt><code>cors.allowed.origins</code></td>
    <dd>
      <p>A list of <a href="http://tools.ietf.org/html/rfc6454">origins</a>
      that are allowed to access the resource. A <code>*</code> can be
      specified to enable access to resource from any origin. Otherwise, a
      whitelist of comma separated origins can be provided. Eg: <code>
      http://www.w3.org, https://www.apache.org</code>.
      <strong>Defaults:</strong> <code>*</code> (Any origin is allowed to
      access the resource).</p>
    </dd>
    <dt><code>cors.allowed.methods</code></td>
    <dd>
      <p>A comma separated list of HTTP methods that can be used to access the
      resource, using cross-origin requests. These are the methods which will
      also be included as part of <code>Access-Control-Allow-Methods</code> 
      header in pre-flight response. Eg: <code>GET, POST</code>.
      <strong>Defaults:</strong> <code>GET, HEAD</code></p>
    </dd>
    <dt><code>cors.allowed.headers</code></td>
    <dd>
      <p>A comma separated list of request headers that can be used when
      making an actual request. These headers will also be returned as part 
      of <code>Access-Control-Allow-Headers</code> header in a pre-flight
      response. Eg: <code>Origin,Accept</code>. <strong>Defaults:</strong>
      <code>Origin, Accept, X-Requested-With, Content-Type,
      Access-Control-Request-Method, Access-Control-Request-Headers</code></p>
    </dd>
    <dt><code>cors.exposed.headers</code></td>
    <dd>
      <p>A comma separated list of headers other than simple response headers
      that browsers are allowed to access. These are the headers which will 
      also be included as part of <code>Access-Control-Expose-Headers</code> 
      header in the pre-flight response. Eg:
      <code>X-CUSTOM-HEADER-PING,X-CUSTOM-HEADER-PONG</code>.
      <strong>Default:</strong> None. Non-simple headers are not exposed by
      default.</p>
    </dd>
    <dt><code>cors.preflight.maxage</code></td>
    <dd>
      <p>The amount of seconds, browser is allowed to cache the result of the
      pre-flight request. This will be included as part of
      <code>Access-Control-Max-Age</code> header in the pre-flight response.
      A negative value will prevent CORS Filter from adding this response
      header to pre-flight response. <strong>Defaults:</strong>
      <code>1800</code></p>
    </dd>
    <dt><code>cors.support.credentials</code></td>
    <dd>
      <p>A flag that indicates whether the resource supports user credentials.
      This flag is exposed as part of
      <code>Access-Control-Allow-Credentials</code> header in a pre-flight
      response. It helps browser determine whether or not an actual request
      can be made using credentials. <strong>Defaults:</strong>
      <code>true</code></p>
    </dd>
    <dt><code>cors.request.decorate</code></td>
    <dd>
      <p>A flag to control if CORS specific attributes should be added to
      HttpServletRequest object or not. <strong>Defaults:</strong>
      <code>true</code></p>
    </dd>
</dl>
      
See [W3C CORS](http://www.w3.org/TR/cors/)
