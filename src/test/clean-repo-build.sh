#!/bin/sh

#exit on any command faliure
set -e 
#set -o pipefail

echo PWD=`pwd`
env

#PATH=$PATH:/opt/java/tools/maven2/current/bin/

#WORKING_DIR=`pwd`
#LOCAL_REPO=$WORKING_DIR/local-repository/
#SETTINGS_FILE=$WORKING_DIR/settings.xml

# Remove existing archetype dir, if present
#rm -rf $WORKING_DIR/test-confluence-plugin

#Remove old settings.xml
#rm -rf $WORKING_DIR/settings.xml

# Remove existing local maven repository, if present
#rm -rf $WORKING_DIR/local-repository/*

#download most recent settings.xml from SVN
#wget http://svn.atlassian.com/svn/public/atlassian/maven-plugins/example-settings-file/settings.xml

# Install Java Activation manually, since we can't host it in Archiva
#mvn -s $SETTINGS_FILE install:install-file -Dmaven.repo.local=$LOCAL_REPO -DgroupId=javax.activation -DartifactId=activation -Dversion=1.0.2 -Dpackaging=jar -Dfile=$WORKING_DIR/lib/activation.jar

# generate template dir from archetype
#mvn -s $SETTINGS_FILE archetype:generate -Dmaven.repo.local=$LOCAL_REPO -DinteractiveMode=false -DarchetypeGroupId=com.atlassian.maven.archetypes -DarchetypeArtifactId=confluence-plugin-archetype -DarchetypeVersion=14 -DremoteRepositories=https://maven.atlassian.com/repository/public/ -DgroupId=com.atlassian.confluence.plugins -DartifactId=test-confluence-plugin

# cd to newly created directory
#cd $WORKING_DIR/test-confluence-plugin

# creating IDEA files, downloading all dependencies
#mvn -s $SETTINGS_FILE idea:idea -Dmaven.repo.local=$LOCAL_REPO

# package plugin, downloading all dependencies
#mvn -s $SETTINGS_FILE package -Dmaven.repo.local=$LOCAL_REPO
