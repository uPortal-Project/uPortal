set JAVADOC_PATH=D:\jdk1.2.2\bin
set JAVADOC_DOCSPATH=D:\Projects\JA-SIG\Portal\docs
set JAVADOC_SOURCEPATH=D:\Projects\JA-SIG\Portal\source

set JAVADOC_CLASSPATH=%JAVADOC_CLASSPATH%;D:\JavaClasses\DXML1_2\lib\dxml.jar
set JAVADOC_CLASSPATH=%JAVADOC_CLASSPATH%;D:\JavaClasses\DXML1_2\lib\xml4j.jar
set JAVADOC_CLASSPATH=%JAVADOC_CLASSPATH%;D:\Projects\JA-SIG\Portal\classes
set JAVADOC_CLASSPATH=%JAVADOC_CLASSPATH%;"D:\Program Files\Tomcat\lib\servlet.jar"

set JAVADOC_WINDOWTITLE="JA-SIG Portal"
set JAVADOC_DOCTITLE="JA-SIG Portal"
set JAVADOC_PACKAGES=org.jasig.portal org.jasig.portal.layout org.jasig.portal.channels org.jasig.portal.channels.rss org.jasig.portal.channels.bookmarks

%JAVADOC_PATH%\javadoc -d %JAVADOC_DOCSPATH% -sourcepath %JAVADOC_SOURCEPATH% -classpath %JAVADOC_CLASSPATH% -splitindex -version -author -windowtitle %JAVADOC_WINDOWTITLE% -doctitle %JAVADOC_DOCTITLE% %JAVADOC_PACKAGES%

pause

