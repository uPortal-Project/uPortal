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

package org.jasig.portal.events;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.jasig.portal.dao.usertype.FunctionalNameType;

/**
 * Constructor assumes that Map passed in is completely immutable
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class PortletExecutionEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;
    
    private final String fname;
    private long executionTime = -1;
    private final long executionTimeNano;
    private final Map<String, List<String>> parameters;

    PortletExecutionEvent() {
        super();
        this.fname = null;
        this.executionTimeNano = -1;
        this.parameters = Collections.emptyMap();
    }

    PortletExecutionEvent(PortalEventBuilder eventBuilder, String fname, long executionTimeNano, Map<String, List<String>> parameters) {
        super(eventBuilder);
        FunctionalNameType.validate(fname);
        Validate.notNull(parameters, "parameters");
        
        this.fname = fname;
        this.executionTimeNano = executionTimeNano;
        this.parameters = parameters;
    }

    /**
     * @return the executionTime in milliseconds
     */
    public long getExecutionTime() {
        long e = this.executionTime;
        if (e == -1) {
            e = TimeUnit.NANOSECONDS.toMillis(this.executionTimeNano);
            this.executionTime = e;
        }
        
        return e;
    }

    /**
     * @return the executionTime in nanoseconds
     */
    public long getExecutionTimeNano() {
        return this.executionTimeNano;
    }
    
    /**
     * @return the fname
     */
    public String getFname() {
        return this.fname;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString() + 
                ", fname=" + this.fname + 
                ", executionTime=" + this.executionTime +
                ", parameters=" + this.parameters.size();
    }
}
