/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal;


/**
 * Base portal exception class.
 * Information contained in this class allows ErrorChannel
 * to handle errors gracefully.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class PortalException extends Exception {

    // should the user be given an option to reinstantiate
    // the channel in a given session ?
    boolean b_reinst=true;
    // should the user be given an option to retry rendering
    // that same channel instance ?
    boolean b_refresh=true;

    // exception trace
    Exception e_exc;

    public PortalException() {
    }

    /**
     * Construct a new portal exception, recording the
     * exception that originally caused the error.
     *
     * @param exc an <code>Exception</code> value
     */
    public PortalException(Exception exc) {
        this.e_exc=exc;
    }

    /**
     * Creates a new <code>PortalException</code> instance,
     * with a contained text message.
     *
     * @param msg a <code>String</code> value
     */
    public PortalException(String msg) {
        super(msg);
    }

    public PortalException(String msg,Exception exc) {
        super(msg);
        this.e_exc=exc;
    }

    public PortalException(String msg, boolean refresh, boolean reinstantiate) {
        super(msg);
        b_reinst=reinstantiate;
        b_refresh=refresh;
    }

    public PortalException(String msg, Exception exc, boolean refresh, boolean reinstantiate) {
        this(msg,refresh,reinstantiate);
        this.e_exc=exc;
    }

    /**
     * Check if user-mediated referesh is allowed.
     *
     * @return a <code>boolean</code> value
     */
    public boolean allowRefresh() {
        return b_refresh;
    }

    /**
     * Check if user-mediated reinstantiation is allowed.
     *
     * @return a <code>boolean</code> value
     */
    public boolean allowReinstantiation() {
        return b_reinst;
    }

    /**
     * Retrieve an optionally recorded exception that
     * caused the error.
     *
     * @return an <code>Exception</code> value
     */
    public Exception getRecordedException() {
        return this.e_exc; 
    }

    /**
     * Set if the user should be presented with an option
     * to retry the same operation on the component that
     * has generated the error.
     *
     * @param refresh a <code>boolean</code> value
     */
    public void setRefreshable(boolean refresh) {
        this.b_refresh=refresh;
    }

    /**
     * Set if the user should be presented with an option
     * to reinstantiate the component (channel) that generated
     * the error.
     *
     * @param reinstantiate a <code>boolean</code> value
     */
    public void setReinstantiable(boolean reinstantiate) {
        this.b_reinst=reinstantiate;
    }

    /**
     * Allows to record the exception that caused the error.
     * The exception information can later be used in error 
     * reporting and user interaction.
     *
     * @param exc an <code>Exception</code> value
     */
    public void setRecordedException(Exception exc) {
        this.e_exc=exc;
    }
    
    /**
     * Override <code>Exception</code> getMessage() method to 
     * append the recorded exception message, if applicable
     *
     * @return the message
     */
    public String getMessage(){
      StringBuffer sb = new StringBuffer(String.valueOf(super.getMessage()));
      if (getRecordedException() !=null){
        sb.append("; "+getRecordedException().getMessage()); 
      }
      return sb.toString();
    }
    
    /**
     * Overrides <code>Exception</code> printStackTrace() method 
     */
    public void printStackTrace(){
      this.printStackTrace(System.out);
    }
    
    /**
     * Overrides <code>Exception</code> printStackTrace(PrintWriter writer) 
     * method to print recorded exception stack trace if applicable
     */
    public void printStackTrace(PrintWriter writer){
      if (getRecordedException()!=null){
        getRecordedException().printStackTrace(writer);
      }
      else{
        super.printStackTrace(writer);
      }
    }
    
    /**
     * Overrides <code>Exception</code> printStackTrace(PrintStream stream) method 
     */
    public void printStackTrace(PrintStream stream){
      this.printStackTrace(new PrintWriter(stream,true));
    }
}
