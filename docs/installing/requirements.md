# Requirements

To build and run uPortal full-source version , you will need the following:

+ **Java 8**
+ **Maven 3.2.2+**
+ **Ant** 1.8.2 or 1.9.3 or later
+ A Servlet container (Tomcat 8.x)
+ A database (e.g. PostgreSQL)

To build and run the uPortal quick-start version (bundled package for simple and quick evaluation; downloaded zip or tar.gz includes self-contained Tomcat/Maven/Ant but you still need to have your own Java installed), download the quick-start version and continue to the Building and Deploying uPortal instructions.  You do not need to do the rest of the items on this page (though you do need your own Java installed and setup as indicated below if you don't already have that).

## Install Java JDK

### Unix/Linux Maven Installation

1. If not already present on your system, download and install Java.
2. Make sure that `JAVA_HOME` is set to the location of your JDK

```shell
export JAVA_HOME=/path/to/java/jdk1.8
```

### Windows Java Installation

1. If not already present on your system, download and install Java.
2. Make sure that `JAVA_HOME` is set to the location of your JDK
    1. open up the system properties (WinKey + Pause)
    2. select the "Advanced" tab
    3. click the "Environment Variables" button
    4. create the `JAVA_HOME` variable in the user variables or system variables with the value `C:\Program Files\Java\jdk1.8` (this directory should be present on your system after Java installation)
3. Update the `Path` environment variable to include `%JAVA_HOME%\bin` (which should come first or early) using the Step 2 instructions.
4. Open a new command prompt (Winkey + R then type cmd) and run `java -version` to verify that it is correctly installed.

## Installing Apache Maven

### Unix/Linux Maven Installation

#### 1. Download Maven

(Do it.)

#### 2. Extract the archive

Extract the archive (`apache-maven-3.2.x-bin.tar.gz`) in the directory you wish to install Maven

```shell_session
tar -xvf apache-maven-3.2.x-bin.tar.gz
```

#### 3. Set JAVA_HOME

Make sure that `JAVA_HOME` is set to the location of your JDK

```shell
export JAVA_HOME=/path/to/java/jdk1.x
```

#### 4. Configure the `M2_HOME` and `MAVEN_OPTS` environment variables

(On Ubuntu, by default Maven is typically found under `/usr/share/maven2` .)

```shell
export M2_HOME=/path/to/apache-maven/apache-maven-3.2.x
export MAVEN_OPTS="-Xmx1024M -XX:MaxPermSize=512m"
```

#### 5. Add Java and Maven to your PATH

```shell
export PATH=$JAVA_HOME/bin:$PATH:$M2_HOME/bin
```

#### 6. Verify

Run the following command to verify that it is correctly installed

```shell_session
mvn --version
```

### Windows Maven Installation

#### 1. Download Maven

(Do it.)

#### 2. Unzip

Unzip the archive, `apache-maven-3.2.x-bin.zip`, to the directory you wish to install Maven (i.e., `C:\Program Files\Apache Software Foundation`)

```shell_session
unzip apache-maven-3.2.x.bin.zip
```

#### 3. Add the `M2_HOME` environment variable

1. open up the system properties (WinKey + Pause)
2. select the "Advanced" tab
3. click the "Environment Variables" button
4. create the `M2_HOME` variable in the user variables with the value `C:\Program Files\Apache Software Foundation\apache-maven-3.2.x`
5. create the `MAVEN_OPTS` variable in the user variables with the value `-Xmx1024M -XX:MaxPermSize=512m`

#### 4. Set JAVA_HOME

Same as above (\*-nix instructions), make sure that `JAVA_HOME` exists in your user variables or in the system variables and set the location of your JDK (`C:\Program Files\Java\jdk1.x`)

#### 5. Update the Path environment variable

Update the Path environment variable to include both ``%JAVA_HOME%\bin` (which should come first or early) and `%M2_HOME%\bin` (which may come last or late) using the Step 3 instructions.

#### 6. Verify

Open a *new* command prompt (Winkey + R then type cmd) and run `mvn --version` to verify that it is correctly installed.


## Installing Apache Ant

### 1. Download Ant

Download Apache Ant (it needs to be version `1.8.2`; you can get that older version of Ant from [the Apache Ant binaries archive](http://archive.apache.org/dist/ant/binaries/) ).

### 2. Extract the archive

Extract the archive in the directory you wish to install Apache Ant

```shell_session
tar -xvf apache-ant-1.8.2-bin.tar.gz
```
### 3. Set `ANT_HOME`

Add the `ANT_HOME` environment variable

```shell
export ANT_HOME=/path/to/apache-ant-1.8.2
```

### 4. Set `JAVA_HOME`

Make sure the `JAVA_HOME` environment variable is setup

```shell
export JAVA_HOME=/usr/local/jdk-1.x
```

### 5. Add `ANT_HOME` to the `PATH`

Add the `ANT_HOME` to the `PATH` variable.

```shell_session
export PATH=$PATH:$ANT_HOME/bin
```
### 6. Verify

Confirm that your Ant installation is working by running the following command

```shell_session
ant -version
```

 
