/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution. 3. Redistributions of any form whatsoever must retain the following acknowledgment:
 * "This product includes software developed by the JA-SIG Collaborative (http://www.jasig.org/)."
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jasig.portal;
import java.io.PrintWriter;
/**
 * A multithreaded version of a {@link ICharacterChannel}.
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>, <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version 1.0
 */
public interface IMultithreadedCharacterChannel extends IMultithreadedChannel {
    /**
     * Asks the channel to render its content as characters.
     * The method has the same call precedence as the IChannel.renderXML() method.
     * (i.e. if the channel also supports ICacheable, portal will try to find a cache entry prior calling this method)
     * @param pw a <code>PrintWriter</code> value into which the character output should be directed
     * @param uid a <code>String</code> identifying the "instance" being served
     * @exception PortalException if an error occurs
     */
    public void renderCharacters(PrintWriter pw, String uid) throws PortalException;
}
