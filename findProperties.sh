#
# Licensed to Jasig under one or more contributor license
# agreements. See the NOTICE file distributed with this work
# for additional information regarding copyright ownership.
# Jasig licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file
# except in compliance with the License. You may obtain a
# copy of the License at:
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on
# an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

mkdir -p target

TARGET_DIR=target
PLACEHOLDERS_PROPS=$TARGET_DIR/placeholders.properties
RESOLVED_PROPS=$TARGET_DIR/resolved_property_names.txt
PORTAL_PROPS=$TARGET_DIR/portal_property_names.txt
MISSING_PROPS=$TARGET_DIR/missing_property_names.txt

## Find all spring property placeholder strings and store them in $PLACEHOLDERS_PROPS
find uportal-war/src/main/resources -name '*.xml' | xargs grep -oh \${[^}]*} | sed 's/\${\([^:]*\):\?\([^}]*\)}/\1=\2/' > $PLACEHOLDERS_PROPS
find uportal-war/src/main/java -name '*.java' | xargs grep -oh "@Value( *\"\${[^}]*}\" *)" | grep -oh \${[^}]*} | sed 's/\${\([^:]*\):\?\([^}]*\)}/\1=\2/' >> $PLACEHOLDERS_PROPS

## Populate $PLACEHOLDERS_PROPS with just the property names 
cat $PLACEHOLDERS_PROPS | sed 's/\([^=]*\).*/\1/' | sort -u  >  $RESOLVED_PROPS

## Pull all of the property names out of portal.properties
cat uportal-war/src/main/resources/properties/portal.properties | grep -v "^#[#| ].*" | grep -v "^#? *$" | sed 's/#\?\([^=]*\).*/\1/' | sort -u  >  $PORTAL_PROPS 

## List all property names that were found in .java and .xml files but do not exist in portal.properties
grep -Fvxf $PORTAL_PROPS $RESOLVED_PROPS > $MISSING_PROPS

## List the full property string found in the source 
grep --color=never -f $MISSING_PROPS $PLACEHOLDERS_PROPS

