/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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