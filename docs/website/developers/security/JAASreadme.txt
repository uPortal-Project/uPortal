other steps to make it work

1) add this line to your <java home>/jre/lib/security/java.security

login.config.url.1=file:<somewhere>/jaas.conf

There is a sample-jaas.conf in the uPortal/properties directory.

2) download and install jaas.jar ( http://java.sun.com/products/jaas/ )
   I am not sure which versions of java (probably 1.3 and above) it supports but I have it working
   on linux ...
      java version "1.3.0_02"
      Java(TM) 2 Runtime Environment, Standard Edition (build 1.3.0_02)
      Java HotSpot(TM) Client VM (build 1.3.0_02, mixed mode)

3) change your uportal security.properties file to reflect the new SecurityContext