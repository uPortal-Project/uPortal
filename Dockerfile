FROM tomcat:8-jre8

ADD build/conf/catalina.properties /usr/local/tomcat/conf/catalina.properties
VOLUME /usr/local/tomcat/portal/

ADD build/docker/ /usr/local/tomcat/
