/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.portlet.rendering;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.concurrent.Callable;
import org.apache.commons.io.output.NullWriter;

/**
 * {@link PrintWriter} that delegates to another {@link PrintWriter} that is retrieved the first
 * time any method is called via the {@link Callable} provided in the constructor.
 */
public class LazyPrintWriter extends PrintWriter {
    private final Callable<PrintWriter> printWriterCreator;
    private PrintWriter printWriter;

    public LazyPrintWriter(Callable<PrintWriter> printWriterCreator) {
        super(NullWriter.NULL_WRITER);

        this.printWriterCreator = printWriterCreator;
    }

    private PrintWriter getPrintWriter() {
        if (this.printWriter == null) {
            try {
                this.printWriter = this.printWriterCreator.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Failed to lazily create PrintWriter", e);
            }
        }

        return this.printWriter;
    }

    @Override
    public void flush() {
        getPrintWriter().flush();
    }

    @Override
    public void close() {
        getPrintWriter().close();
    }

    @Override
    public boolean checkError() {
        return getPrintWriter().checkError();
    }

    @Override
    public void write(int c) {
        getPrintWriter().write(c);
    }

    @Override
    public void write(char[] buf, int off, int len) {
        getPrintWriter().write(buf, off, len);
    }

    @Override
    public void write(char[] buf) {
        getPrintWriter().write(buf);
    }

    @Override
    public void write(String s, int off, int len) {
        getPrintWriter().write(s, off, len);
    }

    @Override
    public void write(String s) {
        getPrintWriter().write(s);
    }

    @Override
    public void print(boolean b) {
        getPrintWriter().print(b);
    }

    @Override
    public void print(char c) {
        getPrintWriter().print(c);
    }

    @Override
    public void print(int i) {
        getPrintWriter().print(i);
    }

    @Override
    public void print(long l) {
        getPrintWriter().print(l);
    }

    @Override
    public void print(float f) {
        getPrintWriter().print(f);
    }

    @Override
    public void print(double d) {
        getPrintWriter().print(d);
    }

    @Override
    public void print(char[] s) {
        getPrintWriter().print(s);
    }

    @Override
    public void print(String s) {
        getPrintWriter().print(s);
    }

    @Override
    public void print(Object obj) {
        getPrintWriter().print(obj);
    }

    @Override
    public void println() {
        getPrintWriter().println();
    }

    @Override
    public void println(boolean x) {
        getPrintWriter().println(x);
    }

    @Override
    public void println(char x) {
        getPrintWriter().println(x);
    }

    @Override
    public void println(int x) {
        getPrintWriter().println(x);
    }

    @Override
    public void println(long x) {
        getPrintWriter().println(x);
    }

    @Override
    public void println(float x) {
        getPrintWriter().println(x);
    }

    @Override
    public void println(double x) {
        getPrintWriter().println(x);
    }

    @Override
    public void println(char[] x) {
        getPrintWriter().println(x);
    }

    @Override
    public void println(String x) {
        getPrintWriter().println(x);
    }

    @Override
    public void println(Object x) {
        getPrintWriter().println(x);
    }

    @Override
    public PrintWriter printf(String format, Object... args) {
        return getPrintWriter().printf(format, args);
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {
        return getPrintWriter().printf(l, format, args);
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        return getPrintWriter().format(format, args);
    }

    @Override
    public PrintWriter format(Locale l, String format, Object... args) {
        return getPrintWriter().format(l, format, args);
    }

    @Override
    public PrintWriter append(CharSequence csq) {
        return getPrintWriter().append(csq);
    }

    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
        return getPrintWriter().append(csq, start, end);
    }

    @Override
    public PrintWriter append(char c) {
        return getPrintWriter().append(c);
    }
}
