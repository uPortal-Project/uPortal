# SSL configuration

The following document assumes that your Java Virtual Machine has already been successfully installed.

The commands regarding creating the certificate keystore reference the `keytool` utility bundled with the Oracle JDK.

+ [Keytool documentation for Windows](http://download.oracle.com/javase/6/docs/technotes/tools/windows/keytool.html)
+ [Keytool documentation for Solaris/Linux](http://download.oracle.com/javase/6/docs/technotes/tools/solaris/keytool.html)

## Step 1: Create a certificate keystore

A certificate keystore is a single file that contains SSL private keys and certificates. Before you can configure Apache Tomcat to listen on https, you must create a certificate keystore that contains a private key and public certificate. Execute the following command:

```shell
$JAVA_HOME/bin/keytool -genkey -alias tomcat -keyalg RSA
```

+ You will be prompted for the "keystore password" which has a default value of `changeit`.
+ The next several prompts will be used to generate a self signed certificate for you brand new private key. If you are familiar with openssl, the fields are presented to you in reverse order:
+ What is your first and last name? (This corresponds to CN and should match the domain name your customers will use to access your uPortal instance, Example: <strong>yourhost.university.edu</strong>)
+ What is the name of your organizational unit? (OU, Example: <strong>Division of Information Technology</strong>)
+ What is the name of your organization? (O, Example: <strong>University of Somewhere</strong>)
+ What is the name of your City or Locality? (L, Example: <strong>Somewhere</strong>)
+ What is the name of your State or Province? (ST, Example: <strong>Wisconsin</strong>)
+ What is the two-letter country code for this unit? (C, Example: <strong>US</strong>)
+ You will be asked to confirm your choices, type `yes` and hit enter to accept.
+ `Enter key password for &lt;tomcat&gt;` is the next question, DO NOT type a password different than your keystore password. Tomcat has no support for keys within keystores that have different password values than the keystore itself. Simply hit enter to proceed.

Your `cacerts` keystore now contains a private key and a self signed certificate. The `cacerts` files can be found inside your JVM install at the path:

```
$JAVA_HOME/jre/lib/security/cacerts
```

Before you publish this uPortal instance to your customers, it is strongly recommended that you get your certificate signed by an authority that is trusted by your customers` web browsers.

In order to get your certificate signed, you will need to generate a Certificate Signing Request (CSR) for your new private key in the `cacerts` file. This can be done with the following command:

```
$JAVA_HOME/bin/keytool -certreq -alias tomcat -keyalg RSA -file tomcat.csr
```


You will be prompted again for the keystore password (default is `changeit`). You'll find the CSR in the current working directory with the filename `tomcat.csr`.

You are now ready to submit your CSR to your preferred Certificate Authority (CA). It may take time for the CA to respond, so you can proceed to step 2. When the CA responds, follow the instructions at the bottom of the page.

## Step 2: Configure Tomcat to use SSL

Go to your `server.xml` file and open the `server.xml` file for editing. The file should be located at `/path/to/tomcat/conf/server.xml`.

```shell
cd /path/to/tomcat/conf
```

Comment out the following code block for port `8080` to disable plain text HTTP:

```xml
<!-- Define a non-SSL HTTP/1.1 Connector on port 8080
<Connector port="8080" maxHttpHeaderSize="8192"
  maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
  enableLookups="false" redirectPort="8443" acceptCount="100"
  connectionTimeout="20000" disableUploadTimeout="true" /> -->
```

Uncomment the following code block to enable the HTTPS connector on port `8443`:

```xml
<!-- Define a SSL HTTP/1.1 Connector on port 8443 -->
<Connector port="8443" maxHttpHeaderSize="8192"
  maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
  enableLookups="false" disableUploadTimeout="true"
  acceptCount="100" scheme="https" secure="true"
  clientAuth="false" sslProtocol="TLS" />
```

Add the `address` attribute to the HTTPS connector:

```xml
<Connector port="8443" maxHttpHeaderSize="8192" address="192.168.1.1"
           maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
           enableLookups="false" disableUploadTimeout="true"
           acceptCount="100" scheme="https" secure="true"
           clientAuth="false" sslProtocol="TLS" />
```

It is important to consider a proper value for the `address` attribute in the HTTPS connector described above. If you do not specify the `address` attribute on a `Connector`, Tomcat will bind to the default value of `0.0.0.0`, which is a special address that translates to ALL bound IP addresses for the host. It is not uncommon to have multiple IP addresses bound to the host running your uPortal/Tomcat instance, and if you don`t specify the specific IP address to listen on, you may open up the HTTPS connector unintentionally on one of those addresses.

Once you have saved your changes to `server.xml`, simply restart Tomcat:

```shell
$TOMCAT_HOME/bin/shutdown.sh
$TOMCAT_HOME/bin/startup.sh
```

## Addendum: Importing the Signed Certificate

Your CA has finally signed your certificate; store the certificate file somewhere on the file system and execute the following command:

```shell
$JAVA_HOME/bin/keytool -import -alias tomcat -keyalg RSA -file /path/to/your/certificate_reply.crt
```

You will be prompted for the keystore password (default is `changeit`).

You can verify that your certificate is signed by looking at the output of:

```
$JAVA_HOME/bin/keytool -list -alias tomcat -v
```

You should see the CA in the certificate chain.

## Additional references

+ [Tomcat SSL Howto](http://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html)
+ [Apache httpd SSL documentation](http://httpd.apache.org/docs/2.2/ssl/)
+ [Apache httpd SSL FAQ](http://httpd.apache.org/docs/2.2/ssl/ssl_faq.html)
