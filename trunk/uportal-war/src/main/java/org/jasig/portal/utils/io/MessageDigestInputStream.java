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

package org.jasig.portal.utils.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * {@link InputStream} wrapper that feeds all read data into the provided {@link MessageDigest}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MessageDigestInputStream extends FilterInputStream {
    private final MessageDigest messageDigest;

    public MessageDigestInputStream(MessageDigest messageDigest, InputStream in) {
        super(in);
        this.messageDigest = messageDigest;
    }

    public MessageDigest getMessageDigest() {
        return this.messageDigest;
    }

    @Override
    public int read() throws IOException {
        final int b = super.read();
        this.messageDigest.update((byte) b);
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException {
        this.messageDigest.update(b);
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.messageDigest.update(b, off, len);
        return super.read(b, off, len);
    }
}
