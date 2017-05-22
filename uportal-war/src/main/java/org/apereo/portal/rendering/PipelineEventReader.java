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

import java.util.Map;

/**
 * Event reader for the rendering pipeline.
 *
 * @param <R> The type of the native event reader
 * @param <E> The type of event the reader exposes
 */
public interface PipelineEventReader<R, E> extends Iterable<E> {
    /** @return The native event reader */
    public R getEventReader();

    /**
     * Get an output property in effect for the rendering pipeline
     *
     * @param name Name of the property
     * @return Value of the property, null if not set
     */
    public String getOutputProperty(String name);

    /** @return A read-only map of all output properties */
    public Map<String, String> getOutputProperties();
}
