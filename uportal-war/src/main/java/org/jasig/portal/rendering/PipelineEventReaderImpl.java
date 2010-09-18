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

package org.jasig.portal.rendering;

import java.util.Iterator;

/**
 * Generic {@link PipelineEventReader} implementation
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PipelineEventReaderImpl<R, E> implements PipelineEventReader<R, E> {
    protected final R eventReader;
    
    public PipelineEventReaderImpl(R eventReader) {
        this.eventReader = eventReader;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<E> iterator() {
        return (Iterator<E>)this.eventReader;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineEventReader#getNativeEventReader()
     */
    @Override
    public R getEventReader() {
        return this.eventReader;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.eventReader.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return this.eventReader.equals(obj);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.eventReader.toString();
    }
}
