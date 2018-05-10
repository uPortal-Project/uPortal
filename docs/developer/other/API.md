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

### Single portlet JSON by fname

`/api/portlet/{fname}.json`

(example: `http://localhost:8080/uPortal/api/portlet/what-is-uportal.json` )

If the requesting user can `BROWSE` the portlet with the requested `fname`, responds with JSON 
representing the `portlet` with that fname. (Note structure in example below.)

Data elements:

Naming and describing the portlet:

 * `title`: End-user-facing name of the portlet. (Not to be confused with administrator-facing 
   `name`)
 * `fname`: technical unique-within-portal identifier for the portlet publication. Typically human 
   readable.
 * `description` : End-user-facing description of the portlet.
 * `iconUrl` : URL to an icon suitable for adorning or representing the portlet registrty entry.
 * `faIcon` : (may be null) identifier of a Font Awesome uicon suitable for adorning or representing
    the portlet registry entry.

About conveying arbitrary configuration:

 * `parameters` : a JavaScript map of name-value pairs representing the portlet publishing parameters
    of the portlet

About escaping the portlet container:

 * `altMaxUrl` : (boolean) true if there is a URL to open in lieu of attempting to render the 
   portlet in the regular way. Useful for portlet registry entries that are lightweight stubs for 
   content external to the portlet container. 
 * `target` : (may be null) indicates what special target, if any, the `altMaxUrl` should open in,
    e.g. `_blank`

About enabling special handling in `uPortal-home`:

 * `widgetType`: (may be null) indicates a widget type, enabling re-using common widget 
   templates
 * `widgetTemplate`: (may be null) indicates a custom template for rendering the widget, used for
   the `custom` `widgetType`.
 * `widgetConfig`: (may be null) JSON configuration that informs the rendering of the widget type or
    custom widget template. This configuration is shared across all usages of the widget in a given 
    portlet definition.
 * `widgetURL` : (may be null) URL from which the browser should read additional JSON to inform the 
   rendering of the widget. Typically this JSON drives the dynamic portions of the custom widget 
   template or the widget type rendering.
 * `staticContent` : (may be null) the static HTML content associated with a simple Simple Content
   Portlet usage, suitable for direct client-side presentation to the user when attempting to render
   this portlet.
 * `pithyStaticContent` : (may be null) IN PRACTICE UNUSED.
 * `renderOnWeb` : (boolean) when true indicates the portlet is suitable for special rendering in
   `EXCLUSIVE` window state and then direct rendering the resulting markup by injecting it into the 
   DOM client-side rather than relying upon the full traditional XSLT rendering pipeline.

Data elements not understood offhand by the author of this documentation:

 * `nodeId` : (This is an opportunity to improve this documentation.)
 * `url` : ditto.

Example response:

```json
{
  "portlet": {
    "nodeId": "-1",
    "title": "Welcome to uPortal",
    "description": "Description of uPortal.",
    "url": null,
    "iconUrl": "/ResourceServingWebapp/rs/tango/0.8.90/32x32/mimetypes/text-html.png",
    "faIcon": null,
    "fname": "what-is-uportal",
    "target": null,
    "widgetURL": null,
    "widgetType": null,
    "widgetTemplate": null,
    "widgetConfig": null,
    "staticContent": "\n            \n                <h2>What is uPortal?</h2>\n                \n                <p>\n                    <a href=\"http://www.apereo.org/uportal\" target=\"_blank\">uPortal</a>\n                    is a free and open source Java-implemented web portal \n                    platform developed and maintained by participants drawn \n                    from across higher education under the coordination of \n                    <a href=\"http://www.apereo.org/\" target=\"_blank\">Apereo</a>.\n                    uPortal can aggregate content, present self-service \n                    applications, personalize presentation and content on the \n                    basis of groups and user attributes, drive mobile device applications, and allow advanced\n                    end-user-participatory customization of the portal experience. \n                    uPortal supports the JSR-286 and JSR-168 Java portlet specification for\n                    including your custom applications within the portal.\n                </p>\n                \n                <p>Welcome to uPortal.</p> \n            \n        ",
    "pithyStaticContent": null,
    "parameters": {
      "mobileIconUrl": "/uPortal/media/skins/icons/mobile/feedback.png",
      "iconUrl": "/ResourceServingWebapp/rs/tango/0.8.90/32x32/mimetypes/text-html.png",
      "disableDynamicTitle": "true",
      "configurable": "true"
    },
    "renderOnWeb": false,
    "altMaxUrl": false
  }
}
```
