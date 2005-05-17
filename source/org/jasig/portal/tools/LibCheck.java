/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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