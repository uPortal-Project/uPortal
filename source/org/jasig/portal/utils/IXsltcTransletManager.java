/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.utils;

import javax.xml.transform.sax.SAXTransformerFactory;

public interface IXsltcTransletManager {
    public boolean transletExists(String uriStr);
    public boolean isDebug();
    public String getPackageName();
    public SAXTransformerFactory getTransformerFactoryImpl();
}
