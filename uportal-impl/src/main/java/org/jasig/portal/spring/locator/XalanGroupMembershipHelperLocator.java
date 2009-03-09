/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.spring.locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.xslt.IXalanGroupMembershipHelper;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XalanGroupMembershipHelperLocator extends AbstractBeanLocator<IXalanGroupMembershipHelper> {
    public static final String BEAN_NAME = "xalanGroupMembershipHelper";
    
    private static final Log LOG = LogFactory.getLog(XalanGroupMembershipHelperLocator.class);
    private static AbstractBeanLocator<IXalanGroupMembershipHelper> locatorInstance;

    public static IXalanGroupMembershipHelper getXalanGroupMembershipHelper() {
        AbstractBeanLocator<IXalanGroupMembershipHelper> locator = locatorInstance;
        if (locator == null) {
            LOG.info("Looking up bean '" + BEAN_NAME + "' in ApplicationContext due to context not yet being initialized");
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            applicationContext.getBean(XalanGroupMembershipHelperLocator.class.getName());
            
            locator = locatorInstance;
            if (locator == null) {
                LOG.warn("Instance of '" + BEAN_NAME + "' still null after portal application context has been initialized");
                return (IXalanGroupMembershipHelper)applicationContext.getBean(BEAN_NAME, IXalanGroupMembershipHelper.class);
            }
        }
        
        return locator.getInstance();
    }

    public XalanGroupMembershipHelperLocator(IXalanGroupMembershipHelper instance) {
        super(instance, IXalanGroupMembershipHelper.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#getLocator()
     */
    @Override
    protected AbstractBeanLocator<IXalanGroupMembershipHelper> getLocator() {
        return locatorInstance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.locator.AbstractBeanLocator#setLocator(org.jasig.portal.spring.locator.AbstractBeanLocator)
     */
    @Override
    protected void setLocator(AbstractBeanLocator<IXalanGroupMembershipHelper> locator) {
        locatorInstance = locator;
    }
}
