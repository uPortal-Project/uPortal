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

package org.jasig.portal.portlet.rendering.worker;

import java.util.Iterator;
import java.util.Set;

import org.jasig.portal.utils.threading.QualityOfServiceBlockingQueue;

/**
 * QOS based queue that spreads out workers by fname.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletWorkerExecutionQueue extends QualityOfServiceBlockingQueue<String, PortletExecutionCallable<?>> {
    private Iterator<String> keyIterator;
    
    public PortletWorkerExecutionQueue() {
        this.resetKeyIterator();
    }
    
    public PortletWorkerExecutionQueue(int capacity) {
        super(capacity);
        this.resetKeyIterator();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.QualityOfServiceQueue#getElementKey(java.lang.Object)
     */
    @Override
    protected String getElementKey(PortletExecutionCallable<?> e) {
        return e.getPortletFname();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.QualityOfServiceQueue#getNextElementKey()
     */
    @Override
    protected String getNextElementKey() {
        boolean reset = false;
        
        String key;
        do {
            //If we hit the end of the iterator reset it
            if (!this.keyIterator.hasNext()) {
                //Safety check to prevent an infinite loop, only allow a reset once
                if (reset) {
                    throw new IllegalStateException("getNextElementKey() was called but no key could be found that had queued elements: " + this.toString());
                }
                
                reset = true;
                this.resetKeyIterator();
            }
            
            key = this.keyIterator.next();
        } while (this.isKeyEmpty(key));
        
        return key;
    }
    
    protected void resetKeyIterator() {
        final Set<String> keySet = this.getKeySet();
        this.keyIterator = keySet.iterator();
    }
}
