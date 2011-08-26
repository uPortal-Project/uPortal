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

package org.jasig.portal;

import java.util.Date;

/**
 * Base portal exception class.
 * Information contained in this class allows ErrorChannel
 * to handle errors gracefully.
 * This class also reports itself to the ProblemsTable whenever it is instantiated.
 * The Problems servlet displays recently reported PortalExceptions.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class PortalException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 
     * should the user be given an option to reinstantiate
     * the channel in a given session
     */
    boolean reinstantiable = true;
    
    /**
     * should the user be given an option to retry rendering
     * that same channel instance
     */
    boolean refreshable = true;

    /**
     * True if logging is pending on this exception instance
     * (has not yet been logged but potentially will be).  True
     * if all the logging that ought to happen has happened.
     */
    boolean logPending = true;
    
    /**
     * Parameter to the ErrorID's template message.
     */
    String parameter = null;
    
    /**
     * The time at which this PortalException instance was instantiated.
     */
    Date timestamp = new Date();
    
    /**
     * Instantiate a generic PortalException.
     * Instantiating a bare, no-message, no ErrorID, no frills 
     * PortalException is pretty anti-social.  Wouldn't you rather
     * use a constructor that provides more information?
     */
    public PortalException() { 
    }

    /**
     * Construct a new portal exception, recording an
     * underlying cause.
     *
     * @param cause a <code>Throwable</code> causing this exception
     */
    public PortalException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new <code>PortalException</code> instance,
     * with a contained text message.
     *
     * @param msg describes exceptional condition
     */
    public PortalException(String msg) {
        super(msg);
    }
    

    /**
     * Instantiate a PortalException with the given message and underlying cause.
     * @param msg - message describing the error
     * @param cause - underlying cause of the error
     */
    public PortalException(String msg, Throwable cause) {
        super(msg, cause);
    }


    /**
     * Check if user-mediated referesh is allowed.
     * @return true if refresh allowed, false otherwise.
     */
    public boolean isRefreshable() {
        return this.refreshable;
    }
    
    /**
     * Check if user-mediated reinstantiation is allowed.
     * @return true if reinstantiation allowed, false otherwise
     */
    public boolean isReinstantiable() {
        return this.reinstantiable;
    }
    
    /**
     * Set if the user should be presented with an option
     * to retry the same operation on the component that
     * has generated the error.
     *
     * @param refresh a <code>boolean</code> value
     */
    public void setRefreshable(boolean refresh) {
        this.refreshable=refresh;
    }
    
    /**
     * Set if the user should be presented with an option
     * to reinstantiate the component (channel) that generated
     * the error.
     *
     * @param reinstantiate a <code>boolean</code> value
     */
    public void setReinstantiable(boolean reinstantiate) {
        this.reinstantiable=reinstantiate;
    }

    /**
     * Determine whether logging is pending on this PortalException.
     * @return <code>true</code> if the log is pending, otherwise <code>false</code>
     */
    public boolean isLogPending() {
        return this.logPending;
    }

    /**
     * Set whether logging is pending on this PortalException.
     * @param b true if logging is pending
     */
    public void setLogPending(boolean b) {
        this.logPending = b;
    }
    

    /**
     * Get the parameter to the ErrorID template message.
     * @return the parameter
     */
    public String getParameter() {
        return this.parameter;
    }

    /**
     * Set the parameter to the ErrorID template message.
     * @param string - parameter to ErrorID template message.
     */
    public void setParameter(String string) {
        this.parameter = string;
    }

    /**
     * Instantiate a PortalException with the given message and refresh,
     * reinstantiate state.
     * @param msg - message describing the problem
     * @param refresh - whether refresh is appropriate response
     * @param reinstantiate - whether reinstantiate is appropriate response
     */
	public PortalException(String msg, boolean refresh, boolean reinstantiate) {
		super(msg);
		this.setReinstantiable(reinstantiate);
		this.setRefreshable(refresh);
	}

    /**
     * Instantiate a PortalException with the given message, underlying cause,
     * refresh, and reinstantiate state.
     * @param msg - message describing the problem
     * @param cause - underlying cause of problem
     * @param refresh - true if refresh is an appropriate response
     * @param reinstantiate - true if reinstantiate is an appropriate response
     */
	public PortalException(String msg, Throwable cause, 
            boolean refresh, boolean reinstantiate) {
		super(msg, cause);
        this.setReinstantiable(reinstantiate);
        this.setRefreshable(refresh);
	}
		
	/**
     * Get the Date at which this PortalException instance was instantiated.
	 * @return Returns the timestamp.
	 */
	public Date getTimestamp() {
		return this.timestamp;
	}

}
