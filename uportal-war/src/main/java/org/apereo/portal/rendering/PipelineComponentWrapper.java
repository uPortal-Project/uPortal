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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps another {@link PipelineComponent}
 *
 */
public abstract class PipelineComponentWrapper<R, E> implements PipelineComponent<R, E> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected PipelineComponent<R, E> wrappedComponent;

    /** @return The wrapped component */
    public PipelineComponent<R, E> getWrappedComponent() {
        return this.wrappedComponent;
    }

    /** @param wrappedComponent The component to wrap */
    public void setWrappedComponent(PipelineComponent<R, E> wrappedComponent) {
        this.wrappedComponent = wrappedComponent;
    }
}
