/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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