/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.io.xml;

import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Can upgrade an older portal data XML document to a newer format of the same data. Multiple data
 * upgraders may be run in succession to translate very old data into the most recent format.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IDataUpgrader {
    /**
     * @return The {@link PortalDataKey}s this upgrader can operate on
     */
    public Set<PortalDataKey> getSourceDataTypes();
    
    /**
     * Upgrade the external XML data format to a newer format
     * @return true if the caller should handle the importing of the result, false if this class handled it internally.
     */
    public boolean upgradeData(Source source, Result result);
}
