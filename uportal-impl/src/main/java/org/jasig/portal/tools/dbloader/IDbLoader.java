/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
