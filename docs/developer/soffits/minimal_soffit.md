
# Minimal Soffit

A soffit's interaction with the portal is HTTP-based.  It is posible to write a
soffit in any language or platform that can accept, process, and respond to a
connection over HTTP.  At the present time, the creators of Soffit expect to
develop soffits mostly with [Java][] and [Spring Boot][].

## Minimal Soffit Setup Instructions Using Spring Boot

1. Use the [Spring Initializer][] to create a new Spring Boot project with the
   following settings:
    * Gradle Project (recommended)
    * Packaging=*War* (recommended)
    * Dependencies=*Cache* (recommended) & *Web* (required)
    * Additional dependencies you intend to use (optional -- you can add them
      later)
1. Add Soffit as a dependency to your project (see below)
1. Add the `tomcat-embed-jasper` dependency to your project (see below)
1. Add the `@SoffitApplication` annotation to your application class (the one
   annotated with `@SpringBootApplication`)
1. Create the directory path `src/main/webapp/WEB-INF/soffit/`
1. Choose a name for your soffit and create a directory with that name inside
   `/soffit/` (above);  recommended:  use only lowercase letters and dashes
   ('-') in the name
1. Create a `view.jsp` file inside the directory named for your soffit;  add
   your markup (_e.g._ `<h2>Hello World!</h2>`)
1. In `src/main/resources/application.properties`, define the `server.port`
   property and set it to an unused port (like 8090)
1. Run the command `$gradle assemble` to build your application
1. Run the command `$java -jar build/lib/{filename}.war` to start your
   application

That's it!  You now have a functioning, minimal Soffit application running on
`localhost` at `server.port`.

### Adding the Soffit dependency

Gradle Example:

``` gradle
compile("org.jasig.portal:uPortal-soffit-renderer:${soffitVersion}")
```

Maven Example:

``` xml
<dependency>
    <groupId>org.jasig.portal</groupId>
    <artifactId>uPortal-soffit-renderer</artifactId>
    <version>${soffitVersion}</version>
</dependency>
```

### Adding the `tomcat-embed-jasper` dependency

Gradle Example:

``` gradle
configurations {
    providedRuntime
}

[...]

providedRuntime('org.apache.tomcat.embed:tomcat-embed-jasper')
```

[Java]: http://www.oracle.com/technetwork/java/index.html
[Spring Boot]: http://projects.spring.io/spring-boot/
[Spring Initializer]: https://start.spring.io/
