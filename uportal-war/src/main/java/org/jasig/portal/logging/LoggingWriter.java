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

package org.jasig.portal.logging;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writer that writes to a {@link Logger}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LoggingWriter extends Writer {
    private static final String NEWLINE = System.getProperty("line.separator");
    private final StringBuilder builder = new StringBuilder();
    private final Logger logger;
    private final LogLevel appendLevel;
    
    public LoggingWriter(String loggerName, LogLevel appendLevel) {
        this(LoggerFactory.getLogger(loggerName), appendLevel);
    }
    
    public LoggingWriter(Class<?> clazz, LogLevel appendLevel) {
        this(LoggerFactory.getLogger(clazz), appendLevel);
    }
    
    public LoggingWriter(Logger logger, LogLevel appendLevel) {
        Validate.notNull(logger);
        Validate.notNull(appendLevel);
        
        this.logger = logger;
        this.appendLevel = appendLevel;
    }
    
    private void logIfNeeded() {
        while (builder.length() > 0) {
            synchronized (builder) {
                final int newlineIndex = builder.indexOf(NEWLINE);
                if (newlineIndex < 0) {
                    return;
                }
                if (newlineIndex == 0) {
                    builder.delete(0, NEWLINE.length());
                    this.appendLevel.log(this.logger, "");
                }
                
                final String msg = builder.substring(0, newlineIndex);
                builder.delete(0, newlineIndex + NEWLINE.length());
                this.appendLevel.log(this.logger, msg);
            }
        }
    }

    @Override
    public void write(int c) throws IOException {
        synchronized (builder) {
            builder.append((char) c);
        }
        logIfNeeded();
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        synchronized (builder) {
            builder.append(cbuf);
        }
        logIfNeeded();
    }

    @Override
    public void write(String str) throws IOException {
        synchronized (builder) {
            builder.append(str);
        }
        logIfNeeded();
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        synchronized (builder) {
            builder.append(str);
        }
        logIfNeeded();
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        synchronized (builder) {
            builder.append(csq);
        }
        logIfNeeded();
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        synchronized (builder) {
            builder.append(csq, start, end);
        }
        logIfNeeded();
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        synchronized (builder) {
            builder.append(c);
        }
        logIfNeeded();
        return this;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        synchronized (builder) {
            builder.append(cbuf, off, len);
        }
        logIfNeeded();
    }

    @Override
    public void flush() throws IOException {
        logIfNeeded();
    }

    @Override
    public void close() throws IOException {
        synchronized (builder) {
            if (builder.length() > 0 && builder.lastIndexOf(NEWLINE) < builder.length() - NEWLINE.length()) {
                builder.append(NEWLINE);
            }
            logIfNeeded();
        }
    }
}
