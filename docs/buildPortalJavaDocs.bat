set JAVADOC_PATH=D:\jdk1.3.0\bin
set JAVADOC_DOCSPATH=D:\Projects\JA-SIG\uPortal2\docs\JavaDocs
set JAVADOC_SOURCEPATH=D:\Projects\JA-SIG\uPortal2\source

set JAVADOC_CLASSPATH=%JAVADOC_CLASSPATH%;D:\Projects\JA-SIG\uPortal2\classes
set JAVADOC_CLASSPATH=%JAVADOC_CLASSPATH%;D:\JavaClasses\xalan_1_2\xalan.jar
set JAVADOC_CLASSPATH=%JAVADOC_CLASSPATH%;D:\JavaClasses\xalan_1_2\xerces.jar
set JAVADOC_CLASSPATH=%JAVADOC_CLASSPATH%;D:\JavaClasses\log4j-1.0.4\log4j.jar
set JAVADOC_CLASSPATH=%JAVADOC_CLASSPATH%;D:\JavaClasses\Tomcat3_2_1\lib\servlet.jar

set JAVADOC_WINDOWTITLE="uPortal 2.0"
set JAVADOC_DOCTITLE="uPortal 2.0"
set JAVADOC_PACKAGES=org.jasig.portal org.jasig.portal.channels org.jasig.portal.channels.UserPreferences org.jasig.portal.security org.jasig.portal.security.provider org.jasig.portal.utils org.jasig.portal.services org.jasig.portal.tools

%JAVADOC_PATH%\javadoc -d %JAVADOC_DOCSPATH% -sourcepath %JAVADOC_SOURCEPATH% -classpath %JAVADOC_CLASSPATH% -splitindex -version -author -windowtitle %JAVADOC_WINDOWTITLE% -doctitle %JAVADOC_DOCTITLE% %JAVADOC_PACKAGES%

pause

