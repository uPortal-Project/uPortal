/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.dbloader;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Holds mapping information from generic data types for table columns
 * to specific types for different databases.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
 * @version $Revision$
 */
public class DbTypeMapping
{
      String dbName;
      String dbVersion;
      String driverName;
      String driverVersion;
      ArrayList types = new ArrayList();

      public String getDbName() { return dbName; }
      public String getDbVersion() { return dbVersion; }
      public String getDriverName() { return driverName; }
      public String getDriverVersion() { return driverVersion; }
      public ArrayList getTypes() { return types; }

      public void setDbName(String dbName) { this.dbName = dbName; }
      public void setDbVersion(String dbVersion) { this.dbVersion = dbVersion; }
      public void setDriverName(String driverName) { this.driverName = driverName; }
      public void setDriverVersion(String driverVersion) { this.driverVersion = driverVersion; }
      public void addType(Type type) { types.add(type); }

      public String getMappedDataTypeName(String genericDataTypeName)
      {
        String mappedDataTypeName = null;
        Iterator iterator = types.iterator();

        while (iterator.hasNext())
        {
          Type type = (Type)iterator.next();

          if (type.getGeneric().equalsIgnoreCase(genericDataTypeName))
            mappedDataTypeName = type.getLocal();
        }
        return mappedDataTypeName;
      }
}
