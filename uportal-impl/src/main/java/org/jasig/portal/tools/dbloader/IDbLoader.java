/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.dbloader;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Describes a class that is able to drop, create and populate database tables based on XML
 * definition files.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IDbLoader {

    /**
     * Executes database loading based on the provided configuration
     */
    public abstract void process(DbLoaderConfiguration configuration) throws ParserConfigurationException,
            SAXException, IOException;

}
