#!/usr/bin/env groovy

/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

def cl = new CliBuilder(usage: 'groovy findProperties.groovy [-s] [-h] [-d]')
cl.h(longOpt:'help', 'Show usage information and quit')
cl.s(longOpt:'source', 'Show property source')
cl.d(longOpt:'debug', 'Show debugging information')

def opt = cl.parse(args)

if (opt.h) {
    cl.usage();
    return;
}


def activePlaceholders = new TreeMap(); //Track active placeholders from portal.properties
def examplePlaceholders = new TreeMap(); //Track example placeholders from portal.properties
def foundPlaceholders = new TreeMap(); //Track placeholders used in code

//search .properties files for existing placeholders
searchFilePlaceholders(
    new File( 'uportal-war/src/main/resources/properties' ),
    ~/.*\.properties/,
    ~/^([^#= ]+)(=?)(.*)$/,
    activePlaceholders);
searchFilePlaceholders(
    new File( 'uportal-war/src/main/resources/properties' ),
    ~/.*\.properties/,
    ~/^#([^#= ]+)(=)(.*)$/,
    examplePlaceholders);

//Search through spring context files for property placeholders
searchPlaceholders(
    new File( 'uportal-war/src/main/resources/properties/contexts' ),
    ~/.*\.xml/,
    ~/\$\{([^}:]*)(:?)(.*)}/,
    foundPlaceholders);
//Serach through Java files for property placholders in @Value annotations
searchPlaceholders(
    new File( 'uportal-war/src/main/java' ),
    ~/.*\.java/,
    ~/@Value *\( *"\$\{([^}:]*)(:?)(.*)}" *\)/,
    foundPlaceholders);

if (opt.d) {
    println ""
    println "Active Properties Found in portal.properties"
    activePlaceholders.each {
        println "\t" + it.key + "=" + it.value;
    }
    println ""
    println "Example Properties Found in portal.properties"
    examplePlaceholders.each {
        println "\t" + it.key + "=" + it.value;
    }
    println ""
    println "Properties Found in XML and Java files"
    foundPlaceholders.each {
        println "\t" + it.key + "=" + it.value;
    }
    println ""
}


def existingPlaceholders = new TreeMap();
existingPlaceholders.putAll(examplePlaceholders);
existingPlaceholders.putAll(activePlaceholders);

def fatalPlaceholders = new TreeMap(); //Placeholders that are missing from properties file and have no default value
def missingPlaceholders = new TreeMap(); //Placeholders that are missing from properties file
def incorrectPlaceholders = new TreeMap(); //Placeholders that exist in the properties file but have differing default values
def shouldntBeActivePlaceholders = new TreeMap(); //Placeholders that exist and are not commented out but are set to their default value

//Process the search results into the missing, incorrect and not-used sets
foundPlaceholders.each {
    existingValue = existingPlaceholders.remove(it.key);
    activeValue = activePlaceholders.get(it.key);
    if (!it.value.hasValue && activeValue == null) {
        //No default value for the placeholder and no active property configured, this is a fatal problem
        fatalPlaceholders.put(it.key, it.value);
    }
    else if (existingValue == null) {
        //Placeholder has a default but no matching entry in portal.properties, record it as missing
        missingPlaceholders.put(it.key, it.value);
    }
    else if (it.value.hasValue) {
        if (existingValue.value != it.value.value) {
            //Placeholder has default value that doesn't match the value in portal.properties, record it as incorrect
            it.value.oldValue = existingValue.value
            incorrectPlaceholders.put(it.key, it.value);
        }
        else if (activeValue != null && activeValue.value == it.value.value) {
            //Placeholder has a default value which matches the active (uncommented) value in portal.properties
            shouldntBeActivePlaceholders.put(it.key, it.value);
        }
    }
}

println ""
if (!existingPlaceholders.isEmpty()) {
    println "Properties in portal.properties that are not referenced"
    existingPlaceholders.each {
        println "\t" + it.key + "=" + it.value.value;
        if (opt.s) {
            println "\t\t" + it.value.source;
        }
    }
    println ""
}
if (!missingPlaceholders.isEmpty()) {
    println "Properties that are missing from portal.properties"
    missingPlaceholders.each {
        println "\t" + it.key + "=" + it.value.value
        if (opt.s) {
            println "\t\t" + it.value.source;
        }
    }
    println ""
}
if (!shouldntBeActivePlaceholders.isEmpty()) {
    println "Properties that are not commented out where the default value matches the property value"
    shouldntBeActivePlaceholders.each {
        println "\t" + it.key + "=" + it.value.value;
        if (opt.s) {
            println "\t\t" + it.value.source;
        }
    }
    println ""
}
if (!incorrectPlaceholders.isEmpty()) {
    println "Properties with incorrect default values in portal.properties (placeholder value)!=(portal.properties value)"
    incorrectPlaceholders.each {
        println "\t" + it.key + "=(" + it.value.value + ")!=(" + it.value.oldValue + ")";
        if (opt.s) {
            println "\t\t" + it.value.source;
        }
    }
    println ""
}
if (!fatalPlaceholders.isEmpty()) {
    println "FATAL: Placeholders with no default value and no active property"
    fatalPlaceholders.each {
        println "\t" + it.key + "=" + it.value.value
        println "\t\t" + it.value.source;
    }
    println ""
}

/**
 * Recursively search a directory for matching files, extracting placeholder data and storing
 * it into the placeholders map
 */
def searchPlaceholders(baseDir, xmlPattern, placeholderPattern, placeholders) {
    searchFilePlaceholders(baseDir, xmlPattern, placeholderPattern, placeholders);
    baseDir.eachDirRecurse() { dir ->
        searchFilePlaceholders(dir, xmlPattern, placeholderPattern, placeholders);
    }
}

/**
 * Search a directory for matching files, extracting placeholder data and storing it into
 * the placeholders map
 */
def searchFilePlaceholders(dir, fileMatcher, placeholderPattern, placeholders) {
    dir.eachFileMatch(fileMatcher) { file ->
        file.eachLine { ln ->
            lnMatcher = ( ln =~ placeholderPattern );
            while ( lnMatcher.find() ) {
                def newPlaceholder = [
                    source:     [ file ],
                    key:        lnMatcher[0][1].trim(),
                    hasValue:   lnMatcher[0][2].trim().size() > 0,
                    value:      lnMatcher[0][3]
                ];
            
                existingPlaceholder = placeholders.get(newPlaceholder.key);
                if (existingPlaceholder != null) {
                    if (existingPlaceholder.value == newPlaceholder.value) {
                        //Multiple placeholders found with the same value, just add to the source array
                        existingPlaceholder.source.add(file);
                    }
                    else if (newPlaceholder.hasValue && !existingPlaceholder.hasValue) {
                        //Existing placeholder didn't have a value, new one does, use the valued version
                        placeholders.put(newPlaceholder.key, newPlaceholder);
                    }
                    else if (existingPlaceholder.hasValue && existingPlaceholder.value != newPlaceholder.value) {
                        System.err << "WARNING: Replacing value for property: " + newPlaceholder.key + "\n" + 
                            "\told value: " + existingPlaceholder.value + " from " + existingPlaceholder.source + "\n" +
                            "\tnew value: " + newPlaceholder.value + " from " + newPlaceholder.source + "\n";
                        placeholders.put(newPlaceholder.key, newPlaceholder);
                    }
                }
                else {
                    //Nothing exists, add the placeholder
                    placeholders.put(newPlaceholder.key, newPlaceholder);
                }
            }
        }
    }
}
