What is this file?

This README documents why these JAXP 1.3 jars are here in this folder and what you should do with them.

The short version:

If compiling and deploying under JDK 1.4.x, drop these .jar files into your /lib/endorsed/ directory of your JDK's JRE
and into the /commmon/endorsed/ directory of your Tomcat.

If using JDK 1.5:

You don't need this folder at all if you're running JDK 1.5 as both 
your development environment and your Tomcat environment.  

What are these jars?

These are the JAXP 1.3 jars.  They implement an endorsed extension to the JDK 1.4.x environment.  
They provide a way to bridge from the JDK 1.4 world to the JDK 1.5 world.  These jars include support
for standardized XML parsing, transformations, and DOM Level 3 Documents.


Under what licenses are these .jars distributed?

See /docs/licenses/acknowledgements.txt.  See also the Java.net site where these .jars are distributed.

https://jaxp.dev.java.net/



Where can I read more?

http://jasigch.princeton.edu:9000/display/UPC/Practical+uP25+XML+Changes

http://jasigch.princeton.edu:9000/display/UPC/XML+API+standardization



What JDK should I be using?

While the uPortal 2.5 release is committed to continuing to support JDK 1.4, 
you might still want to go ahead and use JDK 1.5 if at all possible.