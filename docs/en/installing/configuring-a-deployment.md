# Configuring a uPortal Deployment

Most simple uPortal settings can and should be managed outside the `uPortal.war` package itself
because:

  - Sensitive configuration information is best left out of your Git repository (and `uPortal.war`
    is built from the sources in your repo)
  - It's convenient to manage some common settings without a full build-and-deploy cycle
  - It's advantageous to have build artifacts that are environment-independent and reusable

This system is based on the [Spring Environment abstraction][]. Any setting defined in
`portal.properties`, `rdbm.properties`, or `security.properties` can be managed in this way.

Use one or more of the following options for defining deployment-specific settings:

  - Special properties files in the `portal.home` directory (see below).
  - Environment variables
  - System properties (viz. JVM arguments)

All of these approaches will override the default values for settings found in `uPortal.war`.
This list is shown in ascending order of priority:  values defined in a manner later on this list
will override those defined in a manner earlier on the list.

## The `portal.home` Directory

The Spring Environment inside uPortal will look for two files within a special directory called
`portal.home`:

  - `global.properties
  - `uPortal.properties`

Both files are 100% optional.

The default location of `portal.home` is `${catalina.base}/portal`, but you can specify another
location using a `PORTAL_HOME` environment variable.

The `global.properties` file may be sourced by multiple modules in Tomcat (e.g. portlets);  but the
`uPortal.properties` file should be sourced by uPortal exclusively.  If a setting is defined in
both files, the value defined in `uPortal.properties` "wins."

[Spring Environment abstraction]: https://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#beans-environment
