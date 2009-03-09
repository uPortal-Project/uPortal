/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.layout.dlm.processing.ProcessingPipe;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated code that needs an ProcessingPipe should use direct dependency injection where possible
 */
@Deprecated
public class ProcessingPipeLocator extends AbstractBeanLocator<ProcessingPipe> {
    public static final String BEAN_NAME = ProcessingPipe.PROCESSING_PIPE_BEAN_ID;
    
    private static final Log LOG = LogFactory.getLog(ProcessingPipeLocator.class);
    private static AbstractBeanLocator<ProcessingPipe> locatorInstance;

    public static ProcessingPipe getProcessingPipe() {
        AbstractBeanLocator<ProcessingPipe> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(ProcessingPipeLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (ProcessingPipe)applicationContext.getBean(BEAN_NAME, ProcessingPipe.class);
            }
        }
        
        return locator.getInstance();
    }

    public ProcessingPipeLocator(ProcessingPipe instance) {
        super(instance, ProcessingPipe.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<ProcessingPipe> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<ProcessingPipe> locator) {
        locatorInstance = locator;
    }
}
