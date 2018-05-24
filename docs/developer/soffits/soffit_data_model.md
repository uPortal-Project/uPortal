# Soffit Data Model

To be of any use, real soffits must go beyond _Hello World!_  Soffit provides a
rich data model for sharing data from the portal with your application.  There
are (currently) four objects in this data model:

* The [Bearer][] contains information about the user:  _username_,
  _user attributes_, and _group affiliations_ in the portal
* The [PortalRequest][] contains information about the request your soffit is
  filling, like _parameters_, _mode_, and _window state_
* The [Preferences][] object contains a collection of publish-time settings
  for your soffit chosen by the administrator;  these are options you define for
  your needs;  using preferences is optional
* The [Definition][] contains publish-time metadata about your soffit in the
  portal;  these are settings defined by and consumed by the portal itself, like
  _title_ and _chrome style_

## Sharing Data Model Objects with a Soffit

These objects can be shared by the portal with a soffit on each request, but
<strong>none of them will be sent automatically</strong>.  Under the hood, data
model objects are sent to soffits as Jason Web Tokens (JWTs) using HTTP headers.
Web servers place limits (usually configurable) on the size of the header area
for inbound and outbound requests.  The more data model elements sent, the
greater the risk of exceeding this limit.  In typical cases sending all four
elements is somewhat risky;  sending fewer (1, 2, or 3) should be safe.

You can instruct uPortal to send each data model object using a dedicated
<em>portlet preference</em> in the publishing record (metadata) of each soffit.
The default value of each preference is `false`;  set it to
`true` to send the element.

* `Bearer`:  `org.apereo.portal.soffit.connector.SoffitConnectorController.includeAuthorization`
* `PortalRequest`:  `org.apereo.portal.soffit.connector.SoffitConnectorController.includePortalRequest`
* `Preferences`:  `org.apereo.portal.soffit.connector.SoffitConnectorController.includePreferences`
* `Definition`:  `org.apereo.portal.soffit.connector.SoffitConnectorController.includeDefinition`

## Accessing Data Model Objects in a JSP

Each of these objects is defined within the Expression Language (EL) Context in
which your `.jsp` files execute.  Use camel-case spelling to reference them, for
example...

``` jsp
<h2>Hello ${bearer.username}</h2>
```

## The `@SoffitModelAttribute` Annotation

Sometimes the Data Model provided by Soffit is not enough -- sometimes you need
to define your own objects for rendering a JSP.  For Spring Boot-based Soffit
applications, the [@SoffitModelAttribute][] annotation satisfies this need.

### `@SoffitModelAttribute` Examples

Annotate a Spring bean with `@SoffitModelAttribute` to make the entire bean
available within your JSP.

``` java
@SoffitModelAttribute("settings")
@Component
public class Settings {

    public int getMaxNumber() {
        return 100;
    }

}
```

Annotate a method on a Spring bean with `@SoffitModelAttribute` to have Soffit
invoke the method and make the return value available within your JSP.

``` java
@Component
public class Attributes {

    @SoffitModelAttribute("bearerJson")
    public String getBearerJson(Bearer bearer) {
        String rslt = null;
        try {
            rslt = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bearer);
        } catch (JsonProcessingException e) {
            final String msg = "Unable to write the Bearer object to JSON";
            throw new RuntimeException(msg, e);
        }
        return rslt;
    }

}
```

Signatures of methods annotated with `@SoffitModelAttribute` are flexible;  you
may take any, all, or none of the following objects as parameters, in any order:

* `HttpServletRequest`
* `HttpServletResponse`
* `Bearer`
* `PortalRequest`
* `Preferences`
* `Definition`

[Bearer]: ../../../uPortal-soffit/src/main/java/org/apereo/portal/soffit/model/v1_0/Bearer.java
[PortalRequest]: ../../../uPortal-soffit/src/main/java/org/apereo/portal/soffit/model/v1_0/PortalRequest.java
[Preferences]: ../../../uPortal-soffit/src/main/java/org/apereo/portal/soffit/model/v1_0/Preferences.java
[Definition]: ../../../uPortal-soffit/src/main/java/org/apereo/portal/soffit/model/v1_0/Definition.java
[@SoffitModelAttribute]: ../../../uPortal-soffit-renderer/src/main/java/org/apereo/portal/soffit/renderer/SoffitModelAttribute.java
