
# Minimal Soffit

A soffit's interaction with the portal is HTTP-based.  It is posible to write a
soffit in any language or platform that can accept, process, and respond to a
connection over HTTP.  At the present time, the creators of Soffit expect to
develop soffits mostly with [Java][] and [Spring Boot][].

## Minimal Soffit Setup Instructions Using Spring Boot

1.  Use the [Spring Initializer][] to create a new Spring Boot project with the
    following settings:
    * Gradle Project (recommended)
    * Packaging=*War* (recommended)
    * Dependencies=*Cache* (recommended) & *Web* (required)
    * Additional dependencies you intend to use (optional -- you can add them
      later)

    When you're ready, click the `Generate Project` button and download your new
    project source files as a `.tar.gz` or a `.zip`.  Decompress and copy the
    contents of the archive to a good place in your file system.  Open your
    project files in a suitable tool for editing, such as [IntelliJ IDEA][],
    [Eclipse][], or possibly [Atom][].
2.  Add Soffit as a dependency to your project (see _Adding the Soffit dependency_
    below)
3.  Add the `tomcat-embed-jasper` dependency to your project (see _Adding the
    `tomcat-embed-jasper` dependency_ below)
4.  Add the `@SoffitApplication` annotation to your application class (the one
    already annotated with `@SpringBootApplication`) **NOTE:**  remember to add
    `import org.apereo.portal.soffit.renderer.SoffitApplication;` appropriately
    at the top of the file.
5.  Create the directory path `src/main/webapp/WEB-INF/soffit/`
6.  Choose a name for your soffit and create a directory with that name inside
    `/soffit/` (above);  recommended:  use only lowercase letters and dashes
    ('-') in the name
7.  Create a `view.jsp` file inside the directory named for your soffit;  add
    your markup (_e.g._ `<h2>Hello World!</h2>`)
8.  In `src/main/resources/application.properties`, define the `server.port`
    property and set it to an unused port (like 8090)
9.  Run the command `$ ./gradlew assemble` (on \*-nix) or `$ gradlew.bat assemble`
    (on Windows) to build your application
10. Run the command `$ java -jar build/libs/{filename}.war` to start your
    application

That's it!  You now have a functioning, minimal Soffit application running on
`localhost` at `server.port`.

### Adding the Soffit dependency

You will need to modify the project build file in your editor of choice.
**NOTE:**  be sure to specify the correct dependency version;  it may no
longer be `5.0.0-SNAPSHOT` by the time you're reading this guide.

Gradle Example (`build.gradle`):

``` gradle
repositories {
    mavenLocal()  // Add this line if not already present!
    mavenCentral()
}

[...]

compile('org.jasig.portal:uPortal-soffit-renderer:5.0.0-SNAPSHOT')
```

Maven Example (`pom.xml`):

``` xml
<dependency>
    <groupId>org.jasig.portal</groupId>
    <artifactId>uPortal-soffit-renderer</artifactId>
    <version>5.0.0-SNAPSHOT</version>
</dependency>
```

### Adding the `tomcat-embed-jasper` dependency

You will need to modify the project build file in your editor of choice.

Gradle Examplee (`build.gradle`):

``` gradle
configurations {
    providedRuntime  // Add this line if not already present!
}

[...]

providedRuntime('org.apache.tomcat.embed:tomcat-embed-jasper')
```

Maven Example (`pom.xml`):

``` xml
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-jasper</artifactId>
    <scope>provided</scope>
</dependency>
```

[Java]: http://www.oracle.com/technetwork/java/index.html
[Spring Boot]: http://projects.spring.io/spring-boot/
[Spring Initializer]: https://start.spring.io/
[IntelliJ IDEA]: https://www.jetbrains.com/idea/
[Eclipse]: https://eclipse.org/ide/
[Atom]: https://atom.io/
