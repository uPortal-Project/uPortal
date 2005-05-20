/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * Checks that DOM level 3 is present.
 * uPortal requires level 3 of the DOM (org.w3c.dom) APIs.
 * JDK 1.4 shipped with a DOM 2 implementation of this API in its rt.jar, so
 * deployers using JDK 1.4 must override with the JAXP 1.3
 * implementation of these APIs.  Deployers using JDK 1.5 need do nothing as 
 * JAXP 1.3 is included in baseline JDK 1.5.
 * 
 * This check verifies that level 3 of the APIs is available
 * by exercising a method that exists in DOM3 but not in DOM2.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public final class Dom3Check 
    implements ICheck {

    protected final Log log = LogFactory.getLog(getClass());
    
    public CheckResult doCheck() {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.normalizeDocument();
        } catch (NoSuchMethodError noSuchMethod) {
            // deliberately logging without stack trace since 
            // it is the exception message and not the stack trace that matters here.
            log.error("DOM3 check failing because DOM3 method not found: " + noSuchMethod);
            CheckResult failure = CheckResult.createFailure("DOM3 API method Document.normalizeDocument() not found.",
                    "uPortal requires the DOM3 API which you make available under JDK 1.4 by installing "
                    + "the JAXP 1.3 jars into the endorsed directory of your JDK and of  " 
                    + "your servlet container.  See also the README.txt in the /lib/jaxp/ directory.");
            return failure;
        } catch (ParserConfigurationException e) {
            log.error("Dom3Check could not run because it could not obtain a Document at all.", e);
            CheckResult failure = CheckResult.createFailure("Dom3Check could not run because it could not obtain a Document at all: "
                    + e,
                    "uPortal requires the DOM3 API which you make available under JDK 1.4 by installing "
                    + "the JAXP 1.3 jars into the endorsed directory of your JDK and of  " 
                    + "your servlet container.  See also the README.txt in the /lib/jaxp/ directory.");
        }
        
        CheckResult success = CheckResult.createSuccess("Successfully invoked the DOM3 method Document.normalizeDocument()");
        return success;
    }

    public String getDescription() {
        return "Attempts to execute a DOM3 method to verify that DOM3 "
                + "is present.";
    }

    
    
}
