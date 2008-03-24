/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;

/**
 * Provides additional logging for SQL based exceptions that provide chained exceptions via
 * {@link SQLException#getNextException()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SQLNextExceptionLoggerAspect implements Ordered {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private int order = 0;
    
    /* (non-Javadoc)
     * @see org.springframework.core.Ordered#getOrder()
     */
    public int getOrder() {
        return this.order;
    }
    /**
     * @param order the order to set
     */
    public void setOrder(int order) {
        this.order = order;
    }
    
    public void logBatchUpdateExceptions(Throwable t) {
        while (t != null && !(t instanceof SQLException)) {
            t = t.getCause();
        }
        
        if (t instanceof SQLException) {
            SQLException sqle = (SQLException)t;
            
            //If the SQLException is the root chain the results of getNextException as initCauses
            if (sqle.getCause() == null) {
                SQLException nextException;
                while ((nextException = sqle.getNextException()) != null) {
                    sqle.initCause(nextException);
                    sqle = nextException;
                }
            }
            //The SQLException already has a cause so log the results of all getNextException calls
            else {
                while ((sqle = sqle.getNextException()) != null) {
                    this.logger.error("Logging getNextException for root SQLException: " + t, sqle);
                }
            }
        }
    }
}
