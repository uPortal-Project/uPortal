# API Documentation

## REST APIs

uPortal includes an extensive suite of REST APIs that it uses to access and manipulate portal data
from within the UI.  Swagger-based documentation and tooling is available for these APIs, but is
disabled (by default) as a security precaution.

To enable Swagger API documentation, add the following property to `uPortal.properties`:

```properties
org.apereo.portal.rest.swagger.SwaggerConfiguration.enabled=true
```

After restarting the Tomcat container, you can access the Swagger UI at `/uPortal/api/swagger-ui.html`.

![Swagger API documentation](../../images/swagger.png)
