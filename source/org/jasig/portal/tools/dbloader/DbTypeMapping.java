/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.tools.dbloader;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Holds mapping information from generic data types for table columns
 * to specific types for different databases.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd <mark.boyd@engineer.com>
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
