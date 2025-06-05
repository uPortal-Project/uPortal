![uPortal logo](docs/images/uPortal.png)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jasig.portal/uPortal-web/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jasig.portal/uPortal-web)
[![CI](https://github.com/uPortal-Project/uPortal/actions/workflows/CI.yml/badge.svg?branch=master)](https://github.com/uPortal-Project/uPortal/actions/workflows/CI.yml)


<table>
  <tr>
    <td>
      <table>
        <tr>
          <td>
            Quick links
          </td>
          <td>
            <a href="https://www.w3.org/TR/WCAG20/">
              <img src="https://www.w3.org/WAI/wcag2AA-blue-v.svg" alt="WCAG 2 AA Badge">
            </a>
            <br>
            <a href="https://source.android.com/setup/contribute/code-style">
              <img src="https://img.shields.io/badge/code_style-AOSP-green.svg?style=flat" alt="AOSP Code Style">
            </a>
            <br>
            <a href="https://github.com/search?q=topic%3Auportal+topic%3Aportlet&type=Repositories">
              <img src="https://img.shields.io/badge/discover-portlets-blue.svg?style=flat" alt="Discover Portlets">
            </a>
          </td>
        </tr>
      </table>
    </td>
    <td>
      <table>
        <tr>
          <td>
            Join the Conversation
          </td>
          <td>
            <a href="https://groups.google.com/a/apereo.org/forum/#!forum/uportal-user">
              <img src="https://img.shields.io/badge/uPortal-user-green.svg?style=flat" alt="uPortal user mailing list">
            </a>
            <br>
            <a href="https://groups.google.com/a/apereo.org/forum/#!forum/uportal-dev">
              <img src="https://img.shields.io/badge/uPortal-dev-blue.svg?style=flat" alt="uPortal developer mailing list">
            </a>
            <br>
            <a href="https://apereo.slack.com">
              <img src="https://img.shields.io/badge/chat-on_slack-E01765.svg?style=flat" alt="chat on slack">
            </a>
            <br>
            <a href="https://twitter.com/uPortal">
              <img src="https://img.shields.io/twitter/follow/uPortal.svg?style=social&amp;label=Follow" alt="Twitter Follow">
            </a>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>


## About

uPortal is the leading open source enterprise portal framework built by and for
the higher education community. uPortal continues to evolve through
contributions from its global community and is supported by resources, grants,
donations, and memberships fees from academic institutions, commercial
affiliates, and non-profit foundations. uPortal is built on open standards-based
technologies such as Java and XML, and enables easy, standards-based integration
with authentication and security infrastructures, single sign-on secure access,
campus applications, web-based content, and end user customization. uPortal can
easily integrate with other enterprise systems and can be customized for
specific local needs.

### Forever Free!

You may [download uPortal](https://github.com/uPortal-Project/uPortal/releases) and use it
on your site at no cost. Unlike our competitors, uPortal is 100% free open
source software managed by [Apereo](https://www.apereo.org/content/about). Our
community has access to all releases of the uPortal software with absolutely no
costs. We welcome
[contributions from our community](https://github.com/uPortal-Project/uPortal/graphs/contributors)
of all types and sizes.

### Accessible

uPortal strives to conform with
[Web Content Accessibility Guidelines Version 2.0](https://www.w3.org/TR/WCAG20/)
Level AA. The most recent accessibility audit results can be seen in
[UP-4735](https://issues.jasig.org/browse/UP-4735).

## Help and Support

The [uportal-user@apereo.org](https://wiki.jasig.org/display/JSG/uportal-user)
email address is the best place to go with questions related to configuring or
deploying uPortal.

The [uPortal manual](#manual) is a collaborative resource which has more
detailed documentation for each uPortal release.

### Manual

Additional information about uPortal is available in the Manual.

-   [uPortal 5.9 Manual][latest uportal manual]
-   [uPortal 5.8 Manual](https://github.com/uPortal-Project/uPortal/tree/v5.8.2/docs)
-   [uPortal 5.7 Manual](https://github.com/uPortal-Project/uPortal/tree/v5.7.1/docs)
-   [uPortal 5.6 Manual](https://github.com/uPortal-Project/uPortal/tree/v5.6.1/docs)
-   [uPortal 5.5 Manual](https://github.com/uPortal-Project/uPortal/tree/v5.5.1/docs)
-   [uPortal 5.4 Manual](https://github.com/uPortal-Project/uPortal/tree/v5.4.1/docs)
-   [uPortal 5.3 Manual](https://github.com/uPortal-Project/uPortal/tree/v5.3.2/docs)
-   [uPortal 5.2 Manual](https://github.com/uPortal-Project/uPortal/tree/v5.2.3/docs)
-   [uPortal 5.1 Manual](https://github.com/uPortal-Project/uPortal/tree/v5.1.2/docs)
-   [uPortal 5.0 Manual](https://github.com/uPortal-Project/uPortal/tree/v5.0.7/docs)
-   [uPortal 4.3 Manual](https://wiki.jasig.org/display/UPM43/Home)
-   [uPortal 4.2 Manual](https://wiki.jasig.org/display/UPM42/Home)
-   [uPortal 4.1 Manual](https://wiki.jasig.org/display/UPM41/Home)
-   [uPortal 4.0 Manual](https://wiki.jasig.org/display/UPM40/Home)
-   [uPortal 3.2 Manual](https://wiki.jasig.org/display/UPM32/Home)
-   [uPortal 3.1 Manual](https://wiki.jasig.org/display/UPM31/Home)
-   [uPortal 3.0 Manual](https://wiki.jasig.org/display/UPM30/Home)

## Requirements

-   JDK 1.8 - The JRE alone is NOT sufficient, a full JDK is required
-   GIT

## uPortal-start

uPortal is now meant to be deployed via [uPortal-start][], which is responsible for
servlet container (ie Tomcat), DB, and portal configurations. uPortal-start
deals with the low-level configurations and setup, while letting the adopter
focus on the business configuration side of the deployment. However, it is
possible to run uPortal without uPortal-start. The [uPortal manual][latest uportal manual] explains
how.

## Building and Deploying

uPortal uses Gradle for its project configuration and build system. uPortal
comes with a Gradle wrapper if you don't want to install the build tool
(`./gradlew` in the root directory of the repo).

### Gradle tasks

For a full list of Gradle tasks run `./gradlew tasks` from the root directory.

[latest uportal manual]: https://uportal-project.github.io/uPortal
[uportal-start]: https://github.com/uPortal-Project/uPortal-start
