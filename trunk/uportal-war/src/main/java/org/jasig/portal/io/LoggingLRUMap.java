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

package org.jasig.portal.io;

import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LoggingLRUMap extends LRUMap {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    public LoggingLRUMap() {
        super();
    }
    public LoggingLRUMap(int maxSize, boolean scanUntilRemovable) {
        super(maxSize, scanUntilRemovable);
    }
    public LoggingLRUMap(int maxSize, float loadFactor, boolean scanUntilRemovable) {
        super(maxSize, loadFactor, scanUntilRemovable);
    }
    public LoggingLRUMap(int maxSize, float loadFactor) {
        super(maxSize, loadFactor);
    }
    public LoggingLRUMap(int maxSize) {
        super(maxSize);
    }
    public LoggingLRUMap(Map<?, ?> map, boolean scanUntilRemovable) {
        super(map, scanUntilRemovable);
    }
    public LoggingLRUMap(Map<?, ?> map) {
        super(map);
    }


    /* (non-Javadoc)
     * @see org.apache.commons.collections.map.LRUMap#removeLRU(org.apache.commons.collections.map.AbstractLinkedMap.LinkEntry)
     */
    @Override
    protected boolean removeLRU(LinkEntry entry) {
        if (this.logger.isWarnEnabled()) {
            this.logger.warn("Removing LRU entry with key='" + entry.getKey() + "' and value='" + entry.getValue() + "'");
        }
        
        return super.removeLRU(entry);
    }
}
