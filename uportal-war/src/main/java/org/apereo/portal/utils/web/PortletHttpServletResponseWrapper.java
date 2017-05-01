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
package org.apereo.portal.utils.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.io.output.ProxyWriter;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.utils.DelegatingServletOutputStream;

/**
 * Portlet response wrapper. Makes sure the portlet doesn't screw with the portal's response
 *
 * <p>Any mutator method calls are not passed on and logged at warn level. The writer/stream objects
 * discard all data written and log byte/char counts at warn level on close.
 *
 */
public class PortletHttpServletResponseWrapper extends AbstractHttpServletResponseWrapper {
    private final IPortletWindow portletWindow;
    private ServletOutputStream servletOutputStream;
    private PrintWriter printWriter;

    public PortletHttpServletResponseWrapper(
            HttpServletResponse httpServletResponse, IPortletWindow portletWindow) {
        super(httpServletResponse);
        this.portletWindow = portletWindow;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.servletOutputStream == null) {
            final OutputStream out;
            if (logger.isDebugEnabled()) {
                out =
                        new ByteArrayOutputStream() {
                            @Override
                            public void close() throws IOException {
                                super.close();
                                final byte[] data = this.toByteArray();
                                if (data.length > 0) {
                                    logger.warn(
                                            "Ignored {} bytes written to ServletOutputStream by {}\n\n{}",
                                            new Object[] {
                                                data.length, portletWindow, new String(data)
                                            });
                                }
                            }
                        };
            } else {
                out =
                        new CountingOutputStream(NullOutputStream.NULL_OUTPUT_STREAM) {
                            @Override
                            public void close() throws IOException {
                                super.close();
                                final long byteCount = this.getByteCount();
                                if (byteCount > 0) {
                                    logger.warn(
                                            "Ignored {} bytes written to ServletOutputStream by {}, turn on DEBUG logging to see the output",
                                            byteCount,
                                            portletWindow);
                                }
                            }
                        };
            }

            this.servletOutputStream = new DelegatingServletOutputStream(out);
        }

        return this.servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.printWriter == null) {
            if (logger.isDebugEnabled()) {
                this.printWriter =
                        new PrintWriter(
                                new StringWriter() {
                                    @Override
                                    public void close() throws IOException {
                                        super.close();
                                        final String data = this.toString();
                                        if (data.length() > 0) {
                                            logger.warn(
                                                    "Ignored {} chars written to PrintWriter by {}\n\n{}",
                                                    new Object[] {
                                                        data.length(), portletWindow, data
                                                    });
                                        }
                                    }
                                });
            } else {
                this.printWriter =
                        new PrintWriter(
                                new ProxyWriter(NullWriter.NULL_WRITER) {
                                    private long count = 0;

                                    @Override
                                    public void close() throws IOException {
                                        super.close();
                                        if (count > 0) {
                                            logger.warn(
                                                    "Ignored {} chars written to PrintWriter by {}, turn on DEBUG logging to see the output",
                                                    count,
                                                    portletWindow);
                                        }
                                    }

                                    @Override
                                    protected void afterWrite(int n) throws IOException {
                                        count += n;
                                    }
                                });
            }
        }

        return this.printWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.flushBuffer() from {}", portletWindow);
    }

    @Override
    public void resetBuffer() {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.resetBuffer() from {}", portletWindow);
    }

    @Override
    public void reset() {
        this.logger.warn("Ignoring call to HttpServletResponse.reset() from {}", portletWindow);
    }

    @Override
    public void setBufferSize(int size) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.setBufferSize({}) from {}",
                size,
                portletWindow);
    }

    @Override
    public void setContentType(String type) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.setContentType({}) from {}",
                type,
                portletWindow);
    }

    @Override
    public void setStatus(int sc) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.setStatus({}) from {}", sc, portletWindow);
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.setStatus({}, {}) from {}",
                new Object[] {sc, sm, portletWindow});
    }

    @Override
    public void addCookie(Cookie cookie) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.addCookie({}) from {}",
                cookie,
                portletWindow);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.sendError({}, {}) from {}",
                new Object[] {sc, msg, portletWindow});
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.sendError({}) from {}", sc, portletWindow);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.sendRedirect({}) from {}",
                location,
                portletWindow);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.setCharacterEncoding({}) from {}",
                charset,
                portletWindow);
    }

    @Override
    public void setDateHeader(String name, long date) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.setDateHeader({}, {}) from {}",
                new Object[] {name, date, portletWindow});
    }

    @Override
    public void addDateHeader(String name, long date) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.addDateHeader({}, {}) from {}",
                new Object[] {name, date, portletWindow});
    }

    @Override
    public void setHeader(String name, String value) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.setHeader({}, {}) from {}",
                new Object[] {name, value, portletWindow});
    }

    @Override
    public void setContentLength(int len) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.setContentLength({}) from {}",
                len,
                portletWindow);
    }

    @Override
    public void addHeader(String name, String value) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.addHeader({}, {}) from {}",
                new Object[] {name, value, portletWindow});
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.setIntHeader({}, {}) from {}",
                new Object[] {name, value, portletWindow});
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.addIntHeader({}, {}) from {}",
                new Object[] {name, value, portletWindow});
    }

    @Override
    public void setLocale(Locale loc) {
        this.logger.warn(
                "Ignoring call to HttpServletResponse.setLocale({}) from {}", loc, portletWindow);
    }
}
