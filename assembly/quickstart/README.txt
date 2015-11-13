====
    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
====

uPortal Quick-Start Distribution
--------------------------------------------------------------------------------

Purpose
--------------------------------------------------------------------------------
This quick-start release distribution is provided for those who want to get
familiar with uPortal quickly and easily, without having to compile and deploy
code, and without having to set up a database.


Contents
--------------------------------------------------------------------------------
1) uPortal @uportal.version@
2) Tomcat @tomcat.version@
3) Ant @ant.version@
4) Maven @maven.version@


System requirements
--------------------------------------------------------------------------------
- JDK 1.7 or later
- JDK must include tools.jar.
- JAVA_HOME environment variable must be set to a path that does not contain
  spaces.
- Nothing else should be running on ports 8005, 8080, or 8887.
- Path to the @quickstart.name@ folder should not contain spaces.
- On windows an unzip utility OTHER than the Extractor utility that comes with
  the OS is required. See: https://issues.jasig.org/browse/UP-2024


Instructions
--------------------------------------------------------------------------------
For the commands below, the 'ant' command depends on your operating system:
Windows: ant.bat
UNIX:    ant.sh

'ant start' - Starts HSQL and then Tomcat, both are started in the background.
  (That is, under a UNIX-style operating system this is `ant.sh start` ,
   and under a Windows operating system this is `ant.bat` ).

'ant stop' - Stops Tomcat then stops HSQL. 
  (That is, under a UNIX-style operating system this is `ant.sh stop` ,
   and under a Windows operating system this is `ant.bat stop` ).


Using uPortal
--------------------------------------------------------------------------------
Once Tomcat is running you can access uPortal with your browser by entering:
http://localhost:8080/uPortal/

Logging in
----------
You can login to uPortal with the following user name, password combinations:

demo, demo
student, student
faculty, faculty
staff, staff
developer, developer


Notes
--------------------------------------------------------------------------------
-The uPortal source code is included with this distribution. A separate
 build.xml exists in the @uportal.name@ directory that provides Ant targets to
 deploy changes made to the included uPortal source to the quickstart tomcat
 instance. To run Ant targets for the included uPortal source code cd into the
 @uportal.name@ directory. Running the following command from the @uportal.name@
 directory will list available uPortal Ant targets:

../ant -p

- Log messages for uPortal appear in @tomcat.name@/logs/uPortal.log .
- Log files for Tomcat appear in @tomcat.name@/logs

Documentation
--------------------------------------------------------------------------------

The uPortal product manual is maintained as an online wiki at

 https://wiki.jasig.org/display/UPM41/Home


Contact
--------------------------------------------------------------------------------
Please report bugs and suggestions:

 https://issues.jasig.org/browse/UP

 NOTE: If you are reporting a potential security defect,
 PLEASE DO NOT INITIALLY POST YOUR REPORT PUBLICLY in this issue tracker or
 anywhere else.  Instead, please engage the Apereo security contact process.

 https://wiki.jasig.org/display/JSG/Security+Contact+Group

uPortal website:

 http://www.apereo.org/uportal


