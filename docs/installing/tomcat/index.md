# Installing Tomcat

Apache Tomcat is the recommended servlet container to use with uPortal. While uPortal requires a Servlet 3.0-compatible servlet container and another servlet container may be used, most uPortal implementers deploy to Apache Tomcat. Choosing Tomcat 8.x will likely allow uPortal adopters to get the best advice from the community.

See also

+ [Fronting Tomcat with httpd](fronting-with-httpd.md)
+ [Using SSL](ssl-configuration.md)

## Linux/Unix Installation

### 1. Download

[Download](http://tomcat.apache.org/download-80.cgi) Apache Tomcat 8.x.

### 2. Extract

Untar the package as follows:

```shell_session
tar -zxvf apache-tomcat-8.0.33.tar.gz
```

### 3. Rename

*Optionally* rename your install to something more meaningful:

```shell_session
mv apache-tomcat-8.0.33 uportal-tomcat
```

### 4. Set environment variables

Set your environment variables:

```shell
export JAVA_HOME=/path/to/your/java
export TOMCAT_HOME=/path/to/your/tomcat
```

### 5.Test your Tomcat installation

#### a. Start Tomcat

First, start Tomcat

```shell_session
$TOMCAT_HOME/bin/startup.sh
```

#### b. Verify in Browser

Go to http://localhost:8080/

You should see the Apache Tomcat Welcome screen.

#### c. Shut down Tomcat

```shell_session
TOMCAT_HOME/bin/shutdown.sh
```

## Windows Installation

### 1. Download

Download [Apache Tomcat 8.x](http://tomcat.apache.org/download-80.cgi) for Windows.

### 2. Unzip

Unzip the download into a suitable directory. For example, you may unzip the file into the `C:\` directory. This will create a directory like `C:\apache-tomcat-8.x` containing your Tomcat files.

### 3. Set environment variables

You will need to create two environment variables `CATALINA_HOME` and `JAVA_HOME`.

```shell
CATALINA_HOME : C:\apache-tomcat-8.x
JAVA_HOME : C:\Program Files\Java\jdk1.x
```

For Windows (different versions may vary) you can create these environment variables by doing the following: right-click 'My Computer' select properties and then the Advanced tab. Then click Environment Variables and under System variables click New. From here, you can enter the name and value for `CATALINA_HOME` and again for `JAVA_HOME` if it's not already created.

### 4. Start Tomcat

Try starting up Tomcat by running the `C:\apache-tomcat-8.x\bin\startup.bat` batch file. 

### 5. Verify in Browser

Point your browser to http://localhost:8080 and you should see the default Tomcat home page. 

### 6. Shut down Tomcat again

To shutdown the server run `C:\apache-tomcat-8.x\bin\shutdown.bat` batch file.

## Configuring Tomcat for uPortal

### Shared Libraries

uPortal places libraries in `CATALINA_BASE/shared/lib`. The default Tomcat 7 or 8 download does not enable libraries to be loaded from this directory.

To resolve this you must edit `CATALINA_BASE/conf/catalina.properties` and change the line that begins `shared.loader=` to the following:

```properties
shared.loader=${catalina.base}/shared/lib/*.jar
```

Be **absolutely certain** the `shared.loader` property is configured exactly as shown. An extra space character at the end of the line can prevent it from working as intended, which is very difficult to troubleshoot.

### Shared Sessions

Jasig portlets, as well as many other popular JSR-168 and JSR-286 portlets, rely on the ability to share user session data between the portal web application and portlet applications.

To enable this feature for Tomcat 7 or 8, add the `sessionCookiePath="/"` to `CATALINA_BASE/conf/context.xml`.


```xml
<Context sessionCookiePath="/">
```

### Increase Resource Cache Size

uPortal and the typical collection of portlets take a lot of space. Tomcat 8.5 issues warnings about running out of resource cache space. Add the following cache configuration just before the close of the Context node.

```xml
<Resources cachingAllowed="true" cacheMaxSize="100000" />
</Context>
```

### JVM Heap Configuration

uPortal requires a larger than standard `PermGen` space (Java 7 only) and more heap than may be allocated by default. A good conservative set of heap settings are 

```
-XX:MaxPermSize=384m (Java 7 only) -Xmx2048m
```

To add these, create a file called either `setenv.sh` (Linux/Mac) or `setenv.bat` (Windows) in your `CATALINA_HOME/bin` directory and add the configuration as follows. Note for production settings you would typically want more heap space, at least 4GB. See Additional Tomcat Configuration below.

```
JAVA_OPTS="$JAVA_OPTS -XX:+PrintCommandLineFlags -XX:MaxPermSize=384m -Xms1024m -Xmx2048m -Djsse.enableSNIExtension=false"
```

### Required file permissions

Several uPortal webapps write to their deployed webapps folder to add dynamic content to the portal (altering the Respondr Dynamic Skin and managing Attachments uploaded to uPortal are two use cases). Insure the process Tomcat is running as has write access to `CATALINA_BASE/webapps/*` directories. Typically this is done by having the same account tomcat is running as be the same account you use to build and deploy uPortal.


### GZipping HTML 

(Optional but STRONGLY SUGGESTED unless doing it with Apache httpd or external appliance).

Browser-side performance may be improved by GZip-ping downloaded content where appropriate. uPortal 4 already GZips some CSS and JavaScript. uPortal does not, however, GZip the uPortal page itself.

GZipping of HTML content can be performed via Tomcat. To enable this functionality, set `compression="on"` in the in-use Tomcat connector, and optionally set the list of compressable mime types. More information about this feature can be found in the [Tomcat configuration page][].

```xml
<Connector port="8080" protocol="HTTP/1.1"
  connectionTimeout="20000" redirectPort="8443"
  compression="on" 
  compressableMimeType="text/html,text/xml,text/plain,text/css,text/javascript,application/javascript"/>
```

You can optionally specify compressionMinSize or leave it at it's default value of 2048 bytes.

If you are [fronting Tomcat with Apache httpd](fronting-with-httpd.md) or other hardware systems, you may want to do the compression in Apache or those systems instead.

### Tomcat 7/8 parallel startup

(Optional.)

Tomcat 7.0.23+ can be [configured to have multiple webapps start up in parallel][faster Tomcat startup wiki page], reducing server startup time. Set the `startStopThreads` attribute of a `Host` to a value greater than one.

### HTTP Session Timeout

To set the duration of HTTP sessions modify `CATALINA_BASE/conf/web.xml` and change the session-timeout element to the number of minutes desired.

Tomcat's default is 30 minutes.

```xml
<session-config>
  <session-timeout>30</session-timeout>
</session-config>
```

### Further Tomcat Configurations

#### JVM settings

+ [Example JVM settings](https://wiki.jasig.org/display/UPC/JVM+Configurations)
+ [Heap tuning](https://wiki.jasig.org/display/UPC/uPortal+Heap+Tuning)

#### Disabling SSLv3

(This bit is about *outgoing* SSL. [Documentation about incoming SSL configuration](ssl-configuration.md) is elsewhere.)

Some sites have chosen to disable SSLv3 on their CAS server due to various vulnerabilities. That can cause problems with the CAS client used in uPortal being unable to establish an HTTPS connection to the CAS server to validate the service ticket and throwing an exception

```
javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure
```

One solution is to set the protocols used by Java when making SSL connections. You can do this by adding the following property to `JAVA_OPTS` (or `CATALINA_OPTS` if using that):

Oracle Java7: `-Dhttps.protocols="TLSv1,TLSv1.1,TLSv1.2"` 

Your CAS server must be configured to use one of the mentioned protocols or the handshake will fail. If your test CAS server is publicly accessible, you can view which protocols it supports by [testing its domain name via SSL Labs](https://www.ssllabs.com/ssltest/). 

If you run into troubles:

+ [Diagnosing TLS, SSL, and HTTPS](https://blogs.oracle.com/java-platform-group/entry/diagnosing_tls_ssl_and_https)

[Tomcat configuration page]: http://tomcat.apache.org/tomcat-7.0-doc/config/http.html
[faster Tomcat startup wiki page]: http://wiki.apache.org/tomcat/HowTo/FasterStartUp
