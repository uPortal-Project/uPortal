# Fronting Tomcat with Apache httpd

Optional.

There are a plethora of reasons why you may need or desire to run Apache HTTP Server in front of uPortal.

+ Your single sign on implementation requires use of an apache module (e.g. Shibboleth)
+ You wish to load balance multiple instances of Tomcat and don't have existing load balancing technology
+ You prefer to offload SSL to Apache HTTP Server

## Step 1: Configuring Apache Tomcat

In `/path/to/your/apache-tomcat/conf/server.xml`

### Disable default Connector

Comment out the default connector.

```xml
<!-- Define a non-SSL HTTP/1.1 Connector on port 8080
<Connector port="8080" maxHttpHeaderSize="8192"
  maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
  enableLookups="false" redirectPort="8443" acceptCount="100"
  connectionTimeout="20000" disableUploadTimeout="true" URIEncoding="UTF-8"/>
-->
```

### Enable the AJP connector

Uncomment the following connector block (You may adjust the port if you wish).

```xml
<!-- Define an AJP 1.3 Connector on port 8009 -->
<Connector port="8009" address="127.0.0.1"
  enableLookups="false" redirectPort="8443" protocol="AJP/1.3" />
```

It is important to consider a proper value for the `address` attribute in the AJP connector described above. If you don't specify the `address` attribute on a Connector, Tomcat will bind to the default value of `0.0.0.0`, which is a special address that translates to ALL bound IP addresses for the host. It is not uncommon to have multiple IP addresses bound to the host running your uPortal/Tomcat instance, and if you don't specify the specific IP address to listen on, you may open up the AJP connector unintentionally on one of those addresses.

A good choice to use for the AJP connector is localhost, 127.0.0.1 as long as you run Apache on the same host you run Tomcat. If you run Apache and Tomcat on separate hosts, an ideal IP address to bind your AJP Connector is one that is on a private network or otherwise behind a firewall that would only allow the separate host running Apache to connect and forbid all others.

## Step 2: Configuring Apache Http Server

You will need to configure Apache to route requests to the AJP connector you configured in the previous part. You have two options, `mod_jk` and `mod_proxy_ajp`.

`mod_proxy_ajp` is an extension of Apache mod_proxy that implements the AJP protocol. It is bundled with Apache httpd Server 2.2 and later and can be added to your server instance by adding the following options to your `configure` invocation:

```
--enable-proxy --enable-proxy-ajp
```

mod_proxy_ajp offers simple configuration, particularly if you are already familiar with mod_proxy.

mod_jk is officially known as the Apache Tomcat Connector and is an apache module that must be [downloaded separately](http://tomcat.apache.org/connectors-doc/) and compiled against your Apache HTTP Server source. mod_jk has a slightly more complex configuration, but a different feature set than mod_proxy_ajp.

### Option #1 mod_jk

**Note:** Configuring with IIS use this link.... <a href="http://tomcat.apache.org/connectors-doc/reference/iis.html">http://tomcat.apache.org/connectors-doc/reference/iis.html</a>

#### Download

Download [the Apache Tomcat connector](http://tomcat.apache.org/connectors-doc/).

#### httpd.conf

Edit `/path/to/apache/config/httpd.conf`.

##### LoadModule

Locate the `LoadModule` section and make sure you have the `mod_jk` path defined (path may vary).

```
LoadModule jk_module "/usr/lib/httpd/modules/mod_jk.so"
```

#### IfModule

Define the `IfModule` directive

```apache
<IfModule mod_jk.c>
  JkWorkersFile "/path/to/apache/config/workers.properties"
  JkLogFile "/path/to/apache/logs/mod_jk.log"
  JkLogLevel debug
  JkMount /*.jsp worker1
  JkMount /path/to/portal/* worker1
</IfModule>
JkMountCopy All
```

#### workers.properties

Configure the `workers.properties` file ( You may include the `workers.properties` file in the Apache config directory, but the path must match with the `httpd.conf` file where you defined the `JkWorkersFile` path above.)

```
#Below is an example of a workers.properties file.
# Define 1 real worker using ajp13
worker.list=worker1

# Set properties for worker1 (ajp13)
worker.worker1.type=ajp13
# Set host to match the same value you used above for the 'address' attribute for your AJP Connector
worker.worker1.host=127.0.0.1
# Set the port to match the same value you used above for the 'port' attribute for your AJP Connector
worker.worker1.port=8009

# Below may vary as these are just examples of what can be included.
worker.worker1.lbfactor=50
worker.worker1.cachesize=10
worker.worker1.cache_timeout=600
worker.worker1.socket_keepalive=1
worker.worker1.socket_timeout=300

#Below is an example of a workers.properties file.
# Define 1 real worker using ajp13
worker.list=worker1

# Set properties for worker1 (ajp13)
```


### Option \#2 mod_proxy/mod_proxy_ajp

After you have configured Tomcat in Step 1 you will now need to go to your Apache config directory to setup mod_proxy.

```shell
cd /path/to/apache/config
```

Open httpd.conf for editing and uncomment the following modules.

```shell
LoadModule proxy_module       /usr/lib/apache2-prefork/mod_proxy.so
LoadModule proxy_ajp_module   /usr/lib/apache2-prefork/mod_proxy_ajp.so
```

(File path to the `mod_proxy.so` and `mod_proxy_ajp.so` may vary).

You may chose to keep `mod_proxy_ajp` configurations separate by creating a new file (i.e., `mod_proxy_ajp.conf`), but you will need to map this path in your `httpd.conf` file.

```apache
Include /path/to/apache/stuff/mod_proxy_ajp.conf
```

Whether you place your `mod_proxy_ajp` configurations in a separate file or in the `httpd.conf` is entirely up to you, but you will need to include the following information.

```shell
ProxyRequests Off
<Proxy *>
        Order deny,allow
        Deny from all
        Allow from localhost
</Proxy>
ProxyPass 		/ ajp://127.0.0.1:8009/ retry=0
ProxyPassReverse 	/ ajp://127.0.0.1:8009/ retry=0
```

The IP address and port number in the ProxyPass match the port you defined in the Tomcat AJP 1.3 Connector (Step 1).
