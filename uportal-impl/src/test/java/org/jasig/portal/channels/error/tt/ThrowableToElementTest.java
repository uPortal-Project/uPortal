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

package org.jasig.portal.channels.error.tt;

import org.jasig.portal.channels.error.error2xml.IThrowableToElement;
import org.jasig.portal.channels.error.error2xml.ThrowableToElement;

/**
 * Testcase for ThrowableToElement.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ThrowableToElementTest extends AbstractThrowableToElementTest {

    private ThrowableToElement throwableToElement 
        = new ThrowableToElement();
    
    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#getThrowableToElementInstance()
     */
    protected IThrowableToElement getThrowableToElementInstance() {
        return this.throwableToElement;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#supportedThrowable()
     */
    protected Throwable supportedThrowable() {
        return new Throwable("A test throwable.");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#unsupportedThrowable()
     */
    protected Throwable unsupportedThrowable() {
        // there is no unsupported Throwable for this class.
        return null;
    }

}