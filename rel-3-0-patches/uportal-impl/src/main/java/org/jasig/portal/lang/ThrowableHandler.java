/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.lang;
/**
 * The <code>ThrowableHandler</code> interface defines the error
 * management mechanism for handler implementations. Error management
 * implementations may process the reported error in any way necessary
 * but the implementations should not throw any exceptions from this
 * method. By definition, invocation of the <code>handle</code> method
 * means that the error condition is handled by the
 * implementation.<p/>
 *
 * If an error condition should be wrapped and a new exception thrown,
 * the calling code should not use the
 * <code>ThrowableHelper.handle</code> methods to "handle" the
 * error. Instead, the code should create the new error object with
 * the triggering error object captured as its "cause"; see the
 * <code>ThrowableHelper</code> for details.
 * 
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 *
 * @version "$Revision$"
 **/
public interface ThrowableHandler {
    
    /**
     * Handles the error condition specified in the parameters. The
     * handler can use the client class to resolve the error message
     * property name, and generate a localized message from the
     * optional objects. Implementations should not throw any errors
     * from this method, except <code>NullPointerException</code> if
     * the client or error message property name is <code>null</code>.
     *
     * @param client client calling the handle method
     * @param property property name associated with error message
     * @param objects objects associated with the error message, or
     * <code>null</code>
     * @param cause throwable condition which caused the error, or
     * <code>null</code>
     * @throws NullPointerException if client or property is
     * <code>null</code>
     **/
    void handle(Class client, String property, String[] objects, Throwable cause);
}
