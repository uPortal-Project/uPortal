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

When students sign in to a uPortal site, they see campus announcements,
upcoming events, news, and alerts, next to links to the applications they use.
Faculty and staff sign in at the same address and see the content meant for
them.

Schools build these sites with uPortal, an open source framework.

- Sign-in: uPortal uses the school's existing sign-in service. Options include
  Central Authentication Service (CAS) single sign-on, a directory server, and
  Shibboleth. Groups from the directory or from Grouper decide who gets which
  content.
- Customization: each school sets its own theme, layout, and content. People
  can rearrange their own pages and add content from a catalog.
- Custom components: developers add features as web components on the portal
  page. A component can call campus APIs as the signed-in user, through the
  portal's OpenID Connect support.

This repository holds the framework source code. To set up a portal, start with
[uPortal-start][].

### Open source and funding

uPortal is free to [download](https://github.com/uPortal-Project/uPortal/releases)
and use under the [Apache License 2.0](LICENSE). The
[Apereo Foundation](https://www.apereo.org/content/about), a nonprofit, hosts
the project. Developers from schools and companies that use uPortal
[maintain it](https://github.com/uPortal-Project/uPortal/graphs/contributors),
and contributions of any size are welcome. Grants, donations, and membership
fees from academic institutions, commercial affiliates, and nonprofit
foundations fund the project.

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

-   JDK 11 - The JRE alone is NOT sufficient, a full JDK is required
-   GIT

## Related projects

A running portal combines this repository with the following projects.

- uPortal, this repository: the Java framework. It handles sign-in, user
  layouts, groups and permissions, page rendering, and data import and export.
  Releases ship to Maven Central.
- [uPortal-start][]: the starting point for a new portal. It packages uPortal
  with Tomcat, a database, a Central Authentication Service (CAS) server, and a
  set of portlets. A portlet is a small application that fills one block on the
  page. The bundled portlets include Announcements, Calendar, News Reader,
  Notifications, Bookmarks, and Web Proxy. A school copies uPortal-start and
  keeps its own configuration, skin, and data there.
- [uPortal-web-components][]: browser components for the portal page, such as
  the `waffle-menu` app launcher and the content carousel. uPortal-start
  delivers them to the browser through its resource server.

You can run uPortal without uPortal-start. The
[uPortal manual][latest uportal manual] explains how.

## Building and Deploying

uPortal uses Gradle for its project configuration and build system. uPortal
comes with a Gradle wrapper if you don't want to install the build tool
(`./gradlew` in the root directory of the repo).

### Gradle tasks

For a full list of Gradle tasks run `./gradlew tasks` from the root directory.

[latest uportal manual]: https://uportal-project.github.io/uPortal
[uportal-start]: https://github.com/uPortal-Project/uPortal-start
[uportal-web-components]: https://github.com/uPortal-Project/uPortal-web-components
