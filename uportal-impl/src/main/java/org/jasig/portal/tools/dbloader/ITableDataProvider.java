/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.tools.dbloader;

import java.util.Map;

import org.hibernate.mapping.Table;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ITableDataProvider {

    public Map<String, Table> getTables();

    public Map<String, Map<String, Integer>> getTableColumnTypes();

}