#!/bin/bash        
#
# $Id$
# This script will package up a uPortal quickstart distribution
#

if [ -z "$3" ]; then 
  echo usage: $0 release-tag uportal-src-dir old-quickstart-dir
  exit
fi

RELEASE=uPortal_$1-quick-start
SRC=$2
QSDIR=$3

#
# Create root dir for quick-start distribution
#
echo $RELEASE
mkdir $RELEASE
cd $RELEASE

#
# Move in uPortal source distribution and quickstart scripts
# 
#
cp -r $SRC .
cp $SRC/etc/quickstart/* .

#
# Move in quickstart resources from previously release
# (is there a better way to do a quickstart?)
#
cp -r $QSDIR/Ant_1-6-2 .
cp -r $QSDIR/HSQLDB_1-7-2-4 .
cp -r $QSDIR/Tomcat_5-0-28 .


echo "done!"
echo "Now..."
echo "update server.home in uPortal build.properties"
echo "start hsqldb - ant hsql"
echo "set getDatasourceFromJNDI in portal.properties to true
echo "run ant deploy in uPortal home"
echo "start tomcat, ant tomcatstart, and check for no errors in portal.log"
echo "stop HSQL and Tomcat"
echo "run fix-unix-modes.sh"
echo "zip -r uPortal_X-X-X-quick-start uPortal_X-X-X-quick-start"
echo ""
echo "scp the release to www.jasig.org:/var/www/html/ja-sig/uportaldist"
echo "update download.html page."
echo "update cvs.html page."
echo "update index.html with news about releases."
exit
