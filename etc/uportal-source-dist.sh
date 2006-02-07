#!/bin/bash        

#
# $Id$
# This script will package up a uPortal source distribution
#


if [ -z "$1" ]; then 
  echo usage: $0 release-tag
  exit
fi

#
# Export release tag
#
cvs -d:pserver:anonymous@mis105.mis.udel.edu:/home/cvs/jasig login
cvs -d:pserver:anonymous@mis105.mis.udel.edu:/home/cvs/jasig export -r $1 portal

#
# Remove website directory, build.xml and README in ./docs
# These are used to maintain the website and are not
# needed in the distribution.  Is there a better place for these?
#
rm -rf ./portal/docs/website
rm ./portal/docs/build.xml
rm ./portal/docs/README

#
# Remove .cvsignore files. 
#
find . -name ".cvsignore" | xargs rm

#
# Remove .dict files. (should these be moved to the attic?)
#
find . -name "*.dict" | xargs rm

#
# Create JavaDoc
#
cd portal
ant javadoc
mv ./dist/docs/api ./docs/
ant clean
cd ../

#
# Rename model to uPortal_{release-tag}
#
mv portal uPortal_$1

#
# Zip up distribution
#
zip -r uPortal_$1 uPortal_$1

echo "done!"
echo "Now..."
echo "scp the release to www.jasig.org:/var/www/html/ja-sig/uportaldist"
echo "update download.html page."
echo "update cvs.html page."
echo "update index.html with news about releases."
exit
