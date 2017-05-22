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
import java.io.Writer;
import org.apache.commons.io.output.ProxyWriter;

/**
 * {@link Writer} that mimics the UNIX 'tee' command on a pair of {@link Writer}s. Note the {@link
 * #write(char[], int, int)} implementation is synchronized.
 *
 */
public class TeeWriter extends ProxyWriter {
    private Writer branch;

    public TeeWriter(Writer out, Writer branch) {
        super(out);
        this.branch = branch;
    }

    /** Change the branch Writer */
    protected void setBranch(Writer branch) {
        this.branch = branch;
    }

    public void write(int c) throws IOException {
        super.write(c);
        branch.write(c);
    }

    public void write(char[] cbuf) throws IOException {
        super.write(cbuf);
        branch.write(cbuf);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        super.write(cbuf, off, len);
        branch.write(cbuf, off, len);
    }

    public void write(String str) throws IOException {
        super.write(str);
        branch.write(str);
    }

    public void write(String str, int off, int len) throws IOException {
        super.write(str, off, len);
        branch.write(str, off, len);
    }

    public Writer append(CharSequence csq) throws IOException {
        super.append(csq);
        branch.append(csq);
        return this;
    }

    public Writer append(CharSequence csq, int start, int end) throws IOException {
        super.append(csq, start, end);
        branch.append(csq, start, end);
        return this;
    }

    public Writer append(char c) throws IOException {
        super.append(c);
        branch.append(c);
        return this;
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
