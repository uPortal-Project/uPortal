/* Copyright 2001-2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
     * ErrorID categorizing this PortalException instance.
     */
    ErrorID errorID = Errors.legacy;
    
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
        ProblemsTable.store(this);
    }

    /**
     * Construct a new portal exception, recording an
     * underlying cause.
     *
     * @param cause a <code>Throwable</code> causing this exception
     */
    public PortalException(Throwable cause) {
        super(cause);
        ProblemsTable.store(this);
    }

    /**
     * Creates a new <code>PortalException</code> instance,
     * with a contained text message.
     *
     * @param msg describes exceptional condition
     */
    public PortalException(String msg) {
        super(msg);
        ProblemsTable.store(this);
    }
    
    /**
     * Instantiate a PortalException representing an instance of the
     * type of error represented by the given ErrorID.
     * @param errorid - type of error
     */
    public PortalException(ErrorID errorid) {
    	super(errorid.getMessage());
    	this.errorID=errorid;
        ProblemsTable.store(this);
    }

    /**
     * Instantiate a PortalException with the given message and underlying cause.
     * @param msg - message describing the error
     * @param cause - underlying cause of the error
     */
    public PortalException(String msg, Throwable cause) {
        super(msg, cause);
        ProblemsTable.store(this);
    }

    /**
     * Instantiate a PortalException representing an instance of the type of error
     * represented by the given ErrorID, with the given underlying cause.
     * @param errorid - type of error
     * @param cause - underlying cause of error.
     */
	public PortalException(ErrorID errorid, Throwable cause) {
		super(errorid.getMessage(), cause);
		this.errorID=errorid;
        ProblemsTable.store(this);
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
     * Get the ErrorID representing the type of this error.
     * @return the error ID
     */
    public ErrorID getErrorID() {
        return this.errorID;
    }

    /**
     * Set the ErrorID categorizing this PortalException.
     * @param errorID the ErrorID categorizing this PortalException.
     */
    public void setErrorID(ErrorID errorID) {
        this.errorID = errorID;
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
        ProblemsTable.store(this);
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
        ProblemsTable.store(this);
	}
		
	/**
     * Get the Date at which this PortalException instance was instantiated.
	 * @return Returns the timestamp.
	 */
	public Date getTimestamp() {
		return this.timestamp;
	}

}
