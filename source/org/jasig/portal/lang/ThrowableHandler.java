/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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
public interface ThrowableHandler
{
    /**
     * Handles the error condition specified in the parameters. The
     * handler can use the client class to resolve the error message
     * property name, and generate a localized message from the
     * optional objects. Implementations should not throw any errors
     * from this method, except <code>NullPointerException</code> if
     * the client or error message property name is <code>null</code>.
     *
     * @param client client calling the handle method
     *
     * @param property property name associated with error message
     * 
     * @param objects objects associated with the error message, or
     * <code>null</code>
     *
     * @param cause throwable condition which caused the error, or
     * <code>null</code>
     *
     * @throws NullPointerException if client or property is
     * <code>null</code>
     **/
    void handle(
        Class client,
        String property,
        String[] objects,
        Throwable cause
        );
}
