/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
