#!/bin/sh

#exit on any command faliure
set -e 
#set -o pipefail

PATH=$M2_HOME/bin:$JAVA_HOME/bin:$PATH
WORKING_DIR=`pwd`
LOCAL_REPO=$WORKING_DIR/local-repository/

echo "PATH:        $PATH"
echo "WORKING_DIR: $WORKING_DIR"
echo "LOCAL_REPO:  $LOCAL_REPO"

if [ -d $LOCAL_REPO ]; then
    # Remove existing local maven repository, if present
    echo "Removing local repository $LOCAL_REPO"
    rm -rf $LOCAL_REPO/*
elif
    # Create local repository directory since it doesn't exist
    echo "Creating local repository $LOCAL_REPO"
    mkdir -p $LOCAL_REPO
fi

# Run Maven install
echo "Running: 'mvn -Dmaven.repo.local=$LOCAL_REPO install'"
mvn -Dmaven.repo.local=$LOCAL_REPO install
