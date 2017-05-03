/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.output.ProxyOutputStream;

/**
 * {@link OutputStream} that mimics the UNIX 'tee' command on a pair of {@link OutputStream}s. Note
 * the {@link #write(char[], int, int)} implementation is synchronized.
 *
 */
public class TeeOutputStream extends ProxyOutputStream {
    private OutputStream branch;

    public TeeOutputStream(OutputStream out, OutputStream branch) {
        super(out);
        this.branch = branch;
    }

    /** Change the branch OutputStream */
    protected void setBranch(OutputStream branch) {
        this.branch = branch;
    }

    public void write(int c) throws IOException {
        super.write(c);
        branch.write(c);
    }

    public void write(byte[] b) throws IOException {
        super.write(b);
        branch.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        branch.write(b, off, len);
    }

    public void flush() throws IOException {
        super.flush();
        branch.flush();
    }

    public void close() throws IOException {
        try {
            super.close();
        } finally {
            branch.close();
        }
    }

    public String toString() {
        return branch.toString();
    }
}
