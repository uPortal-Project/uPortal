# uPortal Soffits

Soffit is a technology for creating content that runs in [Apereo uPortal][].  It
is intended as an alternative to [JSR-286 portlet development][].

## Why Would I Want Soffits?

You are a Java web application developer.  You are tasked with developing content
for [Apereo uPortal][].

You are not excited about doing Java Portlet development
[in the traditional way][] or even using [Spring Portlet MVC][].  You correctly
conclude that the Java Portlet APIs are _large_, _obtuse_, and _actively
interfere_ with contemporary web development practices and frameworks that you
want to be using.

Apereo Soffit is an alternative approach to producing content for uPortal that
is not based on JSR-286 or the portlet container.

## Topics

1. [Minimal Soffit](minimal_soffit.md)
2. [Publishing a Soffit](publishing_a_soffit.md)
3. [Soffit Data Model](soffit_data_model.md)
4. [Configuration Options](configuration_options.md)

## A Word on Modern Web User Interfaces

Soffit assumes that you want to develop user interfaces using Javascript and
modern frameworks like [React][], [AngularJS][], [Backbone.js][], _etc_.
Normally a Soffit component will render one time;  considerations like state
changes, transactions, persistence, _etc_. are typically handled with Javascript
and REST APIs.

## Sample Soffit Applications

There are several sample applications in [this repo][].

[Apereo uPortal]: https://www.apereo.org/projects/uportal
[JSR-286 portlet development]: https://jcp.org/en/jsr/detail?id=286
[in the traditional way]: http://www.theserverside.com/tutorial/JSR-286-development-tutorial-An-introduction-to-portlet-programming
[Spring Portlet MVC]: http://docs.spring.io/autorepo/docs/spring/3.2.x/spring-framework-reference/html/portlet.html
[React]: https://facebook.github.io/react/
[AngularJS]: https://angularjs.org/
[Backbone.js]: http://backbonejs.org/
[this repo]: https://github.com/drewwills/soffit-samples
