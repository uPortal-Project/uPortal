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

svn export https://www.ja-sig.org/svn/up2/tags/$1 portal


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
echo "scp the release to www.jasig.org:/jasig/htdocs/www/downloads/uportal"
echo "update download.html page."
echo "update index.html with news about releases."
exit
