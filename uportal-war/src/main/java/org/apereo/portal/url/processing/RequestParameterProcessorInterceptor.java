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
package org.apereo.portal.url.processing;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Manages execution of {@link IRequestParameterProcessor}s.
 *
 */
public class RequestParameterProcessorInterceptor extends HandlerInterceptorAdapter {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private List<IRequestParameterProcessor> dynamicRequestParameterProcessors =
            Collections.emptyList();
    private int maxNumberOfProcessingCycles = 10;

    /** @return the dynamicRequestParameterProcessors */
    public List<IRequestParameterProcessor> getDynamicRequestParameterProcessors() {
        return dynamicRequestParameterProcessors;
    }
    /**
     * @param dynamicRequestParameterProcessors the dynamicRequestParameterProcessors to set
     * @throws IllegalArgumentException if the List is null or contains null elements
     */
    public void setDynamicRequestParameterProcessors(
            List<IRequestParameterProcessor> dynamicRequestParameterProcessors) {
        Validate.notNull(
                dynamicRequestParameterProcessors,
                "IRequestParameterProcessor List can not be null");
        Validate.noNullElements(
                dynamicRequestParameterProcessors,
                "IRequestParameterProcessor List can not contain a null element");

        this.dynamicRequestParameterProcessors = dynamicRequestParameterProcessors;
    }
    /** @return the maxNumberOfProcessingCycles */
    public int getMaxNumberOfProcessingCycles() {
        return maxNumberOfProcessingCycles;
    }
    /**
     * Set the max number of round-robin cycles on the controller list after which, if there are
     * still unfinished controllers the procedure is interrupted and a warning is logged, execution
     * will continue though.
     *
     * @param maxNumberOfProcessingCycles The maxNumberOfProcessingCycles to set.
     */
    public void setMaxNumberOfProcessingCycles(int maxNumberOfProcessingCycles) {
        this.maxNumberOfProcessingCycles = maxNumberOfProcessingCycles;
    }

    /**
     * Process the request first with the dynamic processors until all are complete and then the
     * static processors.
     *
     * @see
     *     org.apereo.portal.url.processing.IRequestParameterController#processParameters(org.apereo.portal.url.IWritableHttpServletRequest,
     *     javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        final List<IRequestParameterProcessor> incompleteDynamicProcessors =
                new LinkedList<IRequestParameterProcessor>(this.dynamicRequestParameterProcessors);

        //Loop while there are still dynamic processors to execute
        int cycles = 0;
        while (incompleteDynamicProcessors.size() > 0) {
            //Run all dynamic processors that are not yet complete
            for (final Iterator<IRequestParameterProcessor> processorItr =
                            incompleteDynamicProcessors.iterator();
                    processorItr.hasNext();
                    ) {
                final IRequestParameterProcessor requestParameterProcessor = processorItr.next();

                //If a dynamic processor completes remove it from the list.
                final boolean complete =
                        requestParameterProcessor.processParameters(request, response);
                if (complete) {
                    processorItr.remove();
                }
            }

            cycles++;

            //If the max cycle count is reached log a warning and break out of the dynamic processing loop
            if (cycles >= this.maxNumberOfProcessingCycles) {
                this.logger.warn(
                        incompleteDynamicProcessors.size()
                                + " IDynamicRequestParameterProcessors did not complete processing after "
                                + cycles
                                + " attempts. Execution will continue but this situation should be reviewed. Incomplete Processors="
                                + incompleteDynamicProcessors,
                        new Throwable("Stack Trace"));
                break;
            }
        }

        return true;
    }
}
