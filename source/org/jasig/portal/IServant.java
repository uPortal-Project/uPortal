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

/** An interface that a Servant Channel must implement.  A Servant Channel is capable of providing some type of
 * interactive service within the flow of another Channel's use.  Originally designed for CGroupsManager
 * and CPermissionsManager, which can function both as standalone channels and provide the functions of 
 * selecting groups and people, and assigning permissions to them (respectively) for other channels
 *
 * @author Alex Vigdor - av317@columbia.edu
 * @version $Revision$
 */
public interface IServant {

    /** Allows the Master Channel to ascertain if the Servant has accomplished the requested task 
     * (Note that the way which a certain task is requested is not specified by this interface; 
     * normally it will be documented by a particular IServant and require some particular 
     * runtimeData and/or staticData parameters passed to the Servant by the Master)
     */    
    public boolean isFinished();
    
    /** Many servant channels will fulfil their function
     * by providing some set of 1 or more Objects to the Master
     * Channel.  This method should only be called by the Master once
     * "isFinished" returns true
     */    
    public Object[] getResults();
    
}

