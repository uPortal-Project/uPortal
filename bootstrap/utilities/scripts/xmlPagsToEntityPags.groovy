/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * This file is a groovy script that parses the PAGSGroupStoreConfig.xml and creates entity-PAGS files that
 * can be imported into an existing uPortal database, creates a SQL script to update the references to the
 * XML PAGS groups to entity-PAGS groups, and provides some instructions to the user on updating
 * compositeGroupService.xml to switch from XML PAGS to entity PAGS.
 *
 * This script is best IMHO executed directly by groovy as:
 * groovy xmlPagsToEntityPags.groovy pathToPAGSGroupStoreConfig.xml directoryToWriteEntityFilesTo
 *
 * However it can also be invoked via ant as:
 * ant -Dmaven.test.skip=true -Ddir=directoryToWriteEntityFilesTo data-import
 * in which case to this groovy script it will look as if invoked as
 * groovy -s pathToscript xmlPagsToEntityPags.groovy pathToPAGSGroupStoreConfig.xml directoryToWriteEntityFilesTo
 *
 * @since 4.2.0
 */

import groovy.xml.MarkupBuilder
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.SAXException

// If groovy script is invoked from ant through uPortal's PortalShell, there will be 2 arguments
// at the front we can ignore: -s scriptPath
if (args.length != 2 && args.length != 4) {
    println """
Usage:  groovy xmlPagsToEntityPags.groovy pathToPAGSGroupStoreConfig.xml directoryToWriteEntityFilesTo

Typically:
  pathToPAGSGroupStoreConfig.xml = ../../../uportal-war/src/main/resources/properties/groups/PAGSGroupStoreConfig.xml
  directoryToWriteEntityFilesTo = ../../../uportal-war/src/main/data/myInstitution/pags-group
        where myInstitution is your modified copy of the quickstart data set

Some entities will exist in uportal-war/src/main/data/default_entities/pags-group
but those created in your myInstitution will override them
"""
    System.exit(1)
}

def pagsFile = args[args.length == 2 ? 0 : 2]
def entityDir = args[args.length == 2 ? 1 : 3]

def xmlSlurper = new XmlSlurper()

// Ignore the dtd specified in PAGSGroupStoreConfig.xml.  For some reason I wasn't able to get XmlSlurper
// to resolve the dtd even when specifying its directory in the classpath.
xmlSlurper.setEntityResolver(new EntityResolver() {
    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        if (systemId.contains("PAGSGroupStore.dtd")) {
            return new InputSource(new StringReader(""));
        } else {
            return null;
        }
    }
})
// For some reason, must set disallow-doctype-decl=false when script is run in intelliJ for debugging purposes
xmlSlurper.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)

def pagsXml = xmlSlurper.parse(new File(pagsFile))

// Create a map of the XML Pags Groups key to name values since entity pages uses only the group-name
def pagsGroupNames = new HashMap();
pagsXml.group.each { group ->
    pagsGroupNames.put(group.'group-key'.text(), group.'group-name'.text())
}

// Insure the target folder exists and is writable
new File(entityDir).mkdirs()

// Add a little output in case script is invoked from ant to help user's identify the output from this script.
println """

Running xmlPagsToEntityPags script
----------------------------------
"""

// Build new entity PAGS xml files from the groups in the PAGSXml file

pagsXml.group.each { group ->
    String groupName = group.'group-name'.text();
    // Create a safe filename
    filename = groupName.replace(' ', '_').replaceAll("[^a-zA-Z_0-9]", "") + ".pags-group.xml"
    def entityFile = new File(entityDir, filename)
    println "Creating file $entityFile.absolutePath for XML PAGS group $groupName"

    def writer = new BufferedWriter(new FileWriter(entityFile))
    def builder = new MarkupBuilder(writer)
    builder.mkp.xmlDeclaration(version: '1.0', encoding: 'UTF-8');
    builder.'pags-group'(script: 'classpath://org/jasig/portal/io/import-pags-group_v4-1.crn') {
        name(groupName)
        description(group.'group-description'.text())
        // If there is a members element, create members but use the group-name instead of the group-key.
        group.members.each { membersItem ->
            members() {
                membersItem.'member-key'.each() {
                    if (pagsGroupNames.get(it.text()) != null) {
                        'member-name'(pagsGroupNames.get(it.text()))
                    } else {
                        println "WARN PAGS group ${groupName} has member-key ${it.text()} which does not map to" +
                                " a group in PAGS.  Ignoring invalid member"
                    }
                }
            }
        }
        // If there is a selection test, create a selection-test node
        group.'selection-test'.each { selectionTest ->
            'selection-test'() {
                selectionTest.'test-group'.each { testGroup ->
                    'test-group'() {
                        testGroup.test.each { testItem ->
                            test() {
                                // Handle mobile user agent string of ${mobile.user.agent.regex}. Change tester from
                                // RegexTester to PropertyRegexTester and InvertedRegexTester to PropertyInvertedRegexTester
                                // and use the property string for the property testers.
                                if (testItem.'test-value'.text().contains("{mobile.user.agent.regex}")) {
                                    'attribute-name'(testItem.'attribute-name'.text())
                                    'tester-class'(testItem.'tester-class'.text()
                                            == "org.jasig.portal.groups.pags.testers.RegexTester" ?
                                            "org.jasig.portal.groups.pags.testers.PropertyRegexTester" :
                                            "org.jasig.portal.groups.pags.testers.PropertyInvertedRegexTester")
                                    'test-value'("org.jasig.portal.http.header.userAgent.mobile.regex.pattern")
                                } else {
                                    'attribute-name'(testItem.'attribute-name'.text())
                                    'tester-class'(testItem.'tester-class'.text())
                                    'test-value'(testItem.'test-value'.text())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    writer.close()
}

println """
Typically you can optionally delete the generated XML files that have duplicates in
  uportal-war/src/main/data/default_entities/pags-group
EXCEPT PAGS_Root.pags-group.xml as it is needed to add your custom PAGS groups to the Root PAGS group.

You will also need to insure compositeGroupService.xml specifies Entity PAGS (see below).

If you were on uPortal 4.0 or prior, best bet is to init the DB after adjusting
your fragment-layout files to look good in the Respondr theme.
"""

// Now create a sql file in the current directory to help the user update their DB if they are
// currently on uPortal 4.1+ and don't want to do an initdb

def sqlFile = new File("updatePagsGroupReferences.sql")
def sqlWriter = new BufferedWriter(new FileWriter(sqlFile))

pagsGroupNames.each { k,v ->
    sqlWriter.writeLine "update UP_DLM_EVALUATOR e set e.GROUP_KEY='pags.${v}' where e.GROUP_NAME='${v}' and e.GROUP_KEY='pags.${k}';"
}
sqlWriter.writeLine ""
pagsGroupNames.each { k,v ->
    sqlWriter.writeLine "update UP_GROUP_MEMBERSHIP m set m.MEMBER_KEY='${v}' where m.MEMBER_SERVICE='pags' and m.MEMBER_KEY='${k}';"
}
sqlWriter.writeLine ""
pagsGroupNames.each { k,v ->
    sqlWriter.writeLine "update UP_PERMISSION p set p.PRINCIPAL_KEY='pags.${v}' where p.PRINCIPAL_KEY='pags.${k}';"
}
sqlWriter.close()

// If invoked via ant, entityDir was 'groovy-safed' via a macro.  Clean it up for user display.
def targetDir = entityDir.replaceAll("\\/", "/").replaceAll("\\\\","\\")

println """
If you have an existing uPortal 4.1+ database and you do not want to run initdb, you'll
also need to run the sql file ${sqlFile.absolutePath} on your database to update it.

You're not done!  Don't forget:
1. pre uPortal 4.2.0: update uportal-war/src/main/resources/properties/groups/compositeGroupServices.xml to use
   entity PAGS instead of XML PAGS.  See comments in the file. Entity PAGS enabled by default in uPortal 4.2.0
2. do an ant initdb
   OR
   a) Import the created pags-entity files into your database
      ant -Dmaven.test.skip=true -Ddir=${targetDir} data-import
   b) If upgrading uPortal 4.1+ that used XML PAGS, run the SQL script ${sqlFile.absolutePath} on the db
"""
