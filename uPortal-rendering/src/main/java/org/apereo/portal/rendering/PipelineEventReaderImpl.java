/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rendering;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic {@link PipelineEventReader} implementation
 *
 */
public class PipelineEventReaderImpl<R, E> implements PipelineEventReader<R, E> {
    private final R eventReader;
    private final Map<String, String> outputProperties;

    public PipelineEventReaderImpl(R eventReader) {
        this.eventReader = eventReader;
        this.outputProperties = new LinkedHashMap<String, String>();
    }

    public PipelineEventReaderImpl(R eventReader, Map<String, String> outputProperties) {
        this.eventReader = eventReader;
        this.outputProperties = new LinkedHashMap<String, String>(outputProperties);
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<E> iterator() {
        return (Iterator<E>) this.eventReader;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.rendering.PipelineEventReader#getNativeEventReader()
     */
    @Override
    public R getEventReader() {
        return this.eventReader;
    }

    public String setOutputProperty(String name, String value) {
        if (value == null) {
            return this.outputProperties.remove(name);
        }

        return this.outputProperties.put(name, value);
    }

    @Override
    public String getOutputProperty(String name) {
        return this.outputProperties.get(name);
    }

    @Override
    public Map<String, String> getOutputProperties() {
        return Collections.unmodifiableMap(this.outputProperties);
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
