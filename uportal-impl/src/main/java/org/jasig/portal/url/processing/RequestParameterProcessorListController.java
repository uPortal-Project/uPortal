/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */


package org.jasig.portal.url.processing;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.url.IWritableHttpServletRequest;

/**
 * RequestParameterProcessorListController is a controller that manages an internal set of {@link org.jasig.portal.url.IRequestParameterProcessor}s.
 * The internal processors are invoked in the specified order, possibly multiple times per request round.
 * It also gathers url constructor references from the url contructor providers and contains internal {@link javax.portlet.PortletURL} implementation.
 *
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision: 11918 $
 */
public class RequestParameterProcessorListController implements IRequestParameterController {
    protected List processors=Collections.EMPTY_LIST;
    protected List urlDecorators=Collections.EMPTY_LIST;
    protected IUrlConstructorProviderFactoryService constructorConfiguration=new UrlConstructorProviderFactoryServiceImpl(Collections.EMPTY_SET);
    protected IUrlConstructorProviderFactoryServiceAccessor constructorProviderFactoryServiceAccessor;
    protected IUrlDecoratorListAccessor urlDecoratorListAccessor;
    
    
    protected int maxNumberOfProcessingCycles=10;
        
    public void processParameters(IWritableHttpServletRequest req, HttpServletResponse res) {
        int ncycles=0;
        List incompleteEvaluatingProcessors=new LinkedList();
        List incompletePlainProcessors=new LinkedList();
        
        // associate url constructor configuration with the current request
        constructorProviderFactoryServiceAccessor.setUrlConstructorProviderFactoryService(constructorConfiguration,req);
        // associate url decorators with the current request
        urlDecoratorListAccessor.setUrlDecoratorList(urlDecorators,req);
        
        for (Iterator iter = processors.iterator(); iter.hasNext();) {
            IRequestParameterProcessor processor = (IRequestParameterProcessor) iter.next();
            if(processor.canEvaluateVariables()) {
                incompleteEvaluatingProcessors.add(processor);
            } else {
                incompletePlainProcessors.add(processor);
            }           
        }
        // go through evaluating processors
        while((!incompleteEvaluatingProcessors.isEmpty()) && ncycles<=maxNumberOfProcessingCycles) {
            List updatedIncompleteControllers=new LinkedList();
            for (Iterator iter = incompleteEvaluatingProcessors.iterator(); iter.hasNext();) {
                IRequestParameterProcessor processor = (IRequestParameterProcessor) iter.next();
                if(!processor.processParameters(req,res)) {
                    updatedIncompleteControllers.add(processor);
                }
            }
            incompleteEvaluatingProcessors=updatedIncompleteControllers;
        }
        
        if(!incompleteEvaluatingProcessors.isEmpty()) {
            throw new PortalRuntimeException("unable to process request parameters within "+Integer.toString(maxNumberOfProcessingCycles)+" URL parameter processing cycles");
        }
        
        // go through plain (non-evaluating processors)
        // one pass is sufficient here
        for (Iterator iter = incompletePlainProcessors.iterator(); iter.hasNext();) {
            IRequestParameterProcessor processor = (IRequestParameterProcessor) iter.next();
            processor.processParameters(req,res);
        }
    }

    /**
     * Set an ordered list of {@link IRequestParameterProcessor}s for this context.
     * @param controllers the list of controllers
     */
    public void setProcessors(List controllers) {
        this.processors = controllers;
    }
    
    /**
     * Set an ordered list of {@link IUrlDecorator}s for this context. 
     * @param decorators
     */
    public void setUrlDecorators(List decorators) {
        //TODO: configuration check
        this.urlDecorators=decorators;
    }
    
    /**
     * @param constructorConfiguration a list of url constructors for the current context
     */
    public void setUrlConstructorProviders(Set providers) {
    	constructorConfiguration = new UrlConstructorProviderFactoryServiceImpl(providers);
    }

    public void init(ServletConfig arg0, Map arg1) throws Exception {}

    public void destroy() throws Exception {}

    /**
     * Set the max number of round-robin cycles on the controller list after which, if there
     * are still unfinished controllers the procedure is interrupted.
     * @param maxNumberOfProcessingCycles The maxNumberOfProcessingCycles to set.
     */
    public void setMaxNumberOfProcessingCycles(int maxNumberOfProcessingCycles) {
        this.maxNumberOfProcessingCycles = maxNumberOfProcessingCycles;
    }
    /**
     * @param constructorProviderFactoryServiceAccessor The constructorProviderFactoryServiceAccessor to set.
     */
    public void setConstructorProviderFactoryServiceAccessor(IUrlConstructorProviderFactoryServiceAccessor constructorProviderFactoryServiceAccessor) {
        this.constructorProviderFactoryServiceAccessor = constructorProviderFactoryServiceAccessor;
    }
    /**
     * @param urlDecoratorListAccessor The urlDecoratorListAccessor to set.
     */
    public void setUrlDecoratorListAccessor(IUrlDecoratorListAccessor urlDecoratorListAccessor) {
        this.urlDecoratorListAccessor = urlDecoratorListAccessor;
    }
}
