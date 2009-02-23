/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.dbloader;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ISchemaExport {

    /**
     * @param export If the database should have the SQL executed agaisnt it
     * @param drop If existing database objects should be dropped before creating new objects
     * @param outputFile Optional file to write out the SQL to.
     */
    public void hbm2ddl(boolean export, boolean create, boolean drop, String outputFile);

}