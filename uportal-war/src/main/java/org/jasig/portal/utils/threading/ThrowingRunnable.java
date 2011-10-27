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

package org.jasig.portal.utils.threading;

/**
 * Base Runnable that allows sub-classes to throw any exception
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class ThrowingRunnable implements Runnable {

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public final void run() {
        try {
            this.runWithException();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Error e) {
            throw e;
        }
        catch (Throwable t) {
            new RuntimeException(t);
        }
    }
    
    public abstract void runWithException() throws Throwable;
}