/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.url.processing;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.url.IWritableHttpServletRequest;

/**
 * Manages execution of {@link IDynamicRequestParameterProcessor}s and {@link IStaticRequestParameterProcessor}s.
 * 
 * @author Eric Dalquist
 * @version $Revision: 11918 $
 */
public class RequestParameterProcessorListController implements IRequestParameterController {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private List<IStaticRequestParameterProcessor> staticRequestParameterProcessors = Collections.emptyList();
    private List<IDynamicRequestParameterProcessor> dynamicRequestParameterProcessors = Collections.emptyList();
    private int maxNumberOfProcessingCycles = 10;

    /**
     * @return the staticRequestParameterProcessors
     */
    public List<IStaticRequestParameterProcessor> getStaticRequestParameterProcessors() {
        return staticRequestParameterProcessors;
    }
    /**
     * @param staticRequestParameterProcessors the staticRequestParameterProcessors to set
     * @throws IllegalArgumentException if the List is null or contains null elements
     */
    public void setStaticRequestParameterProcessors(List<IStaticRequestParameterProcessor> staticRequestParameterProcessors) {
        Validate.notNull(staticRequestParameterProcessors, "IStaticRequestParameterProcessor List can not be null");
        Validate.noNullElements(staticRequestParameterProcessors, "IStaticRequestParameterProcessor List can not contain a null element");
        
        this.staticRequestParameterProcessors = staticRequestParameterProcessors;
    }
    /**
     * @return the dynamicRequestParameterProcessors
     */
    public List<IDynamicRequestParameterProcessor> getDynamicRequestParameterProcessors() {
        return dynamicRequestParameterProcessors;
    }
    /**
     * @param dynamicRequestParameterProcessors the dynamicRequestParameterProcessors to set
     * @throws IllegalArgumentException if the List is null or contains null elements
     */
    public void setDynamicRequestParameterProcessors(List<IDynamicRequestParameterProcessor> dynamicRequestParameterProcessors) {
        Validate.notNull(dynamicRequestParameterProcessors, "IDynamicRequestParameterProcessor List can not be null");
        Validate.noNullElements(dynamicRequestParameterProcessors, "IDynamicRequestParameterProcessor List can not contain a null element");
        
        this.dynamicRequestParameterProcessors = dynamicRequestParameterProcessors;
    }
    /**
     * @return the maxNumberOfProcessingCycles
     */
    public int getMaxNumberOfProcessingCycles() {
        return maxNumberOfProcessingCycles;
    }
    /**
     * Set the max number of round-robin cycles on the controller list after which, if there
     * are still unfinished controllers the procedure is interrupted and a warning is logged,
     * execution will continue though. 
     * 
     * @param maxNumberOfProcessingCycles The maxNumberOfProcessingCycles to set.
     */
    public void setMaxNumberOfProcessingCycles(int maxNumberOfProcessingCycles) {
        this.maxNumberOfProcessingCycles = maxNumberOfProcessingCycles;
    }


    /**
     * Process the request first with the dynamic processors until all are complete and then the static processors.
     * 
     * @see org.jasig.portal.url.processing.IRequestParameterController#processParameters(org.jasig.portal.url.IWritableHttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void processParameters(IWritableHttpServletRequest req, HttpServletResponse res) {
        final List<IDynamicRequestParameterProcessor> incompleteDynamicProcessors = new LinkedList<IDynamicRequestParameterProcessor>(this.dynamicRequestParameterProcessors);

        //Loop while there are still dynamic processors to execute
        int cycles = 0;
        while (incompleteDynamicProcessors.size() > 0) {
            //Run all dynamic processors that are not yet complete
            for (final Iterator<IDynamicRequestParameterProcessor> processorItr = incompleteDynamicProcessors.iterator(); processorItr.hasNext(); ) {
                final IDynamicRequestParameterProcessor requestParameterProcessor = processorItr.next();

                //If a dynamic processor completes remove it from the list.
                final boolean complete = requestParameterProcessor.processParameters(req, res);
                if (complete) {
                    processorItr.remove();
                }
            }
            
            cycles++;
            
            //If the max cycle count is reached log a warning and break out of the dynamic processing loop
            if (cycles >= this.maxNumberOfProcessingCycles) {
                this.logger.warn(incompleteDynamicProcessors.size() + " IDynamicRequestParameterProcessors did not completel processing after " + cycles + " attempts. Execution will continue but this situation should be reviewed. Incomplete Processors=" + incompleteDynamicProcessors, new Throwable("Stack Trace"));
                break;
            }
        }
        
        //Run all static processors
        for (final IStaticRequestParameterProcessor requestParameterProcessor : this.staticRequestParameterProcessors) {
            requestParameterProcessor.processParameters(req, res);
        }
    }
}
