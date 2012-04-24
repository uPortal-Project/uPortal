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
package org.jasig.portal.portlet.rendering;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.servlet.ServletOutputStream;

public class LazyServletOutputStream extends ServletOutputStream {
    private final Callable<ServletOutputStream> servletOutputStreamCreator;
    private ServletOutputStream servletOutputStream;

    public LazyServletOutputStream(Callable<ServletOutputStream> servletOutputStreamCreator) {
        this.servletOutputStreamCreator = servletOutputStreamCreator;
    }

    private ServletOutputStream getServletOutputStream() {
        if (this.servletOutputStream == null) {
            try {
                this.servletOutputStream = this.servletOutputStreamCreator.call();
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to lazily create ServletOutputStream", e);
            }
        }

        return this.servletOutputStream;
    }

    public void write(int b) throws IOException {
        getServletOutputStream().write(b);
    }

    public void write(byte[] b) throws IOException {
        getServletOutputStream().write(b);
    }

    public void print(String s) throws IOException {
        getServletOutputStream().print(s);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        getServletOutputStream().write(b, off, len);
    }

    public void print(boolean b) throws IOException {
        getServletOutputStream().print(b);
    }

    public void print(char c) throws IOException {
        getServletOutputStream().print(c);
    }

    public void print(int i) throws IOException {
        getServletOutputStream().print(i);
    }

    public void flush() throws IOException {
        getServletOutputStream().flush();
    }

    public void print(long l) throws IOException {
        getServletOutputStream().print(l);
    }

    public void print(float f) throws IOException {
        getServletOutputStream().print(f);
    }

    public void close() throws IOException {
        getServletOutputStream().close();
    }

    public void print(double d) throws IOException {
        getServletOutputStream().print(d);
    }

    public void println() throws IOException {
        getServletOutputStream().println();
    }

    public void println(String s) throws IOException {
        getServletOutputStream().println(s);
    }

    public void println(boolean b) throws IOException {
        getServletOutputStream().println(b);
    }

    public void println(char c) throws IOException {
        getServletOutputStream().println(c);
    }

    public void println(int i) throws IOException {
        getServletOutputStream().println(i);
    }

    public void println(long l) throws IOException {
        getServletOutputStream().println(l);
    }

    public void println(float f) throws IOException {
        getServletOutputStream().println(f);
    }

    public void println(double d) throws IOException {
        getServletOutputStream().println(d);
    }
}
