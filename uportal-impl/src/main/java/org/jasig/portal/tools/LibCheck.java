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

package org.jasig.portal.tools;

public class LibCheck {
    
    /**
     * Class that checks that JAXP is present in the JVM. This allows us to give a
     * useful error back to the user if we find JAXP missing instead of getting many of
     * class not found errors the user will see a message instructing them how to fix
     * the problem.
     */
    public LibCheck() {
        super();
    }
    
    public static void main(String[] args) {
        String importantClass = "javax.xml.xpath.XPathConstants";
        try {
            Class c = LibCheck.class.getClassLoader().loadClass(importantClass);
        } catch (ClassNotFoundException e) {
            System.err.println(e);
            System.err.println("The missing class is provided as part of JAXP.\n" +
                "Check that you have the JAXP jars installed in your JDK.\n" +
                "For more information see lib/jaxp/README.txt.");
            System.exit(1);
        }
        System.exit(0);
    }
}