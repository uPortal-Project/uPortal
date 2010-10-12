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

package com.ctc.wstx.sw;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.io.Stax2Result;
import org.codehaus.stax2.ri.Stax2EventWriterImpl;
import org.codehaus.stax2.ri.Stax2WriterAdapter;

import com.ctc.wstx.api.WriterConfig;
import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.cfg.OutputConfigFlags;
import com.ctc.wstx.dom.WstxDOMWrappingWriter;
import com.ctc.wstx.exc.WstxIOException;
import com.ctc.wstx.io.CharsetNames;
import com.ctc.wstx.io.UTF8Writer;
import com.ctc.wstx.util.URLUtil;

/**
 * Clone of WstxOutputFactory created solely to return a HtmlStreamWriter
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class HtmlWstxOutputFactory extends XMLOutputFactory2 implements OutputConfigFlags {
    /*
    /////////////////////////////////////////////////////
    // Actual storage of configuration settings
    /////////////////////////////////////////////////////
     */

    protected final WriterConfig mConfig;

    /*
    /////////////////////////////////////////////////////
    // Life-cycle
    /////////////////////////////////////////////////////
     */

    public HtmlWstxOutputFactory() {
        mConfig = WriterConfig.createFullDefaults();
    }

    /*
    /////////////////////////////////////////////////////
    // XMLOutputFactory API
    /////////////////////////////////////////////////////
     */

    @Override
    public XMLEventWriter createXMLEventWriter(OutputStream out) throws XMLStreamException {
        return createXMLEventWriter(out, null);
    }

    @Override
    public XMLEventWriter createXMLEventWriter(OutputStream out, String enc) throws XMLStreamException {
        if (out == null) {
            throw new IllegalArgumentException("Null OutputStream is not a valid argument");
        }
        return new Stax2EventWriterImpl(createSW(out, null, enc, false));
    }

    @Override
    public XMLEventWriter createXMLEventWriter(javax.xml.transform.Result result) throws XMLStreamException {
        return new Stax2EventWriterImpl(createSW(result));
    }

    @Override
    public XMLEventWriter createXMLEventWriter(Writer w) throws XMLStreamException {
        if (w == null) {
            throw new IllegalArgumentException("Null Writer is not a valid argument");
        }
        return new Stax2EventWriterImpl(createSW(null, w, null, false));
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(OutputStream out) throws XMLStreamException {
        return createXMLStreamWriter(out, null);
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(OutputStream out, String enc) throws XMLStreamException {
        if (out == null) {
            throw new IllegalArgumentException("Null OutputStream is not a valid argument");
        }
        return createSW(out, null, enc, false);
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(javax.xml.transform.Result result) throws XMLStreamException {
        return createSW(result);
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(Writer w) throws XMLStreamException {
        if (w == null) {
            throw new IllegalArgumentException("Null Writer is not a valid argument");
        }
        return createSW(null, w, null, false);
    }

    @Override
    public Object getProperty(String name) {
        return mConfig.getProperty(name);
    }

    @Override
    public boolean isPropertySupported(String name) {
        return mConfig.isPropertySupported(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        mConfig.setProperty(name, value);
    }

    /*
    /////////////////////////////////////////
    // Stax2 extensions
    /////////////////////////////////////////
     */

    // // // Stax2 additional (encoding-aware) factory methods

    @Override
    public XMLEventWriter createXMLEventWriter(Writer w, String enc) throws XMLStreamException {
        return new Stax2EventWriterImpl(createSW(null, w, enc, false));
    }

    @Override
    public XMLEventWriter createXMLEventWriter(XMLStreamWriter sw) throws XMLStreamException {
        XMLStreamWriter2 sw2 = Stax2WriterAdapter.wrapIfNecessary(sw);
        return new Stax2EventWriterImpl(sw2);
    }

    @Override
    public XMLStreamWriter2 createXMLStreamWriter(Writer w, String enc) throws XMLStreamException {
        return createSW(null, w, enc, false);
    }

    // // // Stax2 "Profile" mutators

    @Override
    public void configureForXmlConformance() {
        mConfig.configureForXmlConformance();
    }

    @Override
    public void configureForRobustness() {
        mConfig.configureForRobustness();
    }

    @Override
    public void configureForSpeed() {
        mConfig.configureForSpeed();
    }

    /*
    /////////////////////////////////////////
    // Woodstox-specific configuration access
    /////////////////////////////////////////
     */

    public WriterConfig getConfig() {
        return mConfig;
    }

    /*
    /////////////////////////////////////////
    // Internal methods:
    /////////////////////////////////////////
     */

    /**
     * Bottleneck factory method used internally; needs to take care of passing
     * proper settings to stream writer.
     *
     * @param requireAutoClose Whether this result will always require
     *   auto-close be enabled (true); or only if application has
     *   requested it (false)
     */
    private XMLStreamWriter2 createSW(OutputStream out, Writer w, String enc, boolean requireAutoClose)
            throws XMLStreamException {
        /* Need to ensure that the configuration object is not shared
         * any more; otherwise later changes via factory could be
         * visible half-way through output...
         */
        WriterConfig cfg = mConfig.createNonShared();
        XmlWriter xw;

        boolean autoCloseOutput = requireAutoClose || mConfig.willAutoCloseOutput();

        if (w == null) {
            if (enc == null) {
                enc = WstxOutputProperties.DEFAULT_OUTPUT_ENCODING;
            }
            else {
                /* Canonical ones are interned, so we may have
                 * normalized encoding already...
                 */
                if (enc != CharsetNames.CS_UTF8 && enc != CharsetNames.CS_ISO_LATIN1 && enc != CharsetNames.CS_US_ASCII) {
                    enc = CharsetNames.normalize(enc);
                }
            }

            try {
                if (enc == CharsetNames.CS_UTF8) {
                    w = new UTF8Writer(cfg, out, autoCloseOutput);
                    xw = new BufferingXmlWriter(w, cfg, enc, autoCloseOutput, out, 16);
                }
                else if (enc == CharsetNames.CS_ISO_LATIN1) {
                    xw = new ISOLatin1XmlWriter(out, cfg, autoCloseOutput);
                }
                else if (enc == CharsetNames.CS_US_ASCII) {
                    xw = new AsciiXmlWriter(out, cfg, autoCloseOutput);
                }
                else {
                    w = new OutputStreamWriter(out, enc);
                    xw = new BufferingXmlWriter(w, cfg, enc, autoCloseOutput, out, -1);
                }
            }
            catch (IOException ex) {
                throw new XMLStreamException(ex);
            }
        }
        else {
            // we may still be able to figure out the encoding:
            if (enc == null) {
                enc = CharsetNames.findEncodingFor(w);
            }
            try {
                xw = new BufferingXmlWriter(w, cfg, enc, autoCloseOutput, null, -1);
            }
            catch (IOException ex) {
                throw new XMLStreamException(ex);
            }
        }

        //Always use the HtmlStreamWriter, the standard WstxOutputFactory does a bunch of namespace configuration tests here
        return new HtmlStreamWriter(xw, enc, cfg);
    }

    private XMLStreamWriter2 createSW(Result res) throws XMLStreamException {
        OutputStream out = null;
        Writer w = null;
        String encoding = null;
        boolean requireAutoClose;
        String sysId = null;

        if (res instanceof Stax2Result) {
            Stax2Result sr = (Stax2Result) res;
            try {
                out = sr.constructOutputStream();
                if (out == null) {
                    w = sr.constructWriter();
                }
            }
            catch (IOException ioe) {
                throw new WstxIOException(ioe);
            }
            // yes, it's required since caller has no access to stream/writer:
            requireAutoClose = true;
        }
        else if (res instanceof StreamResult) {
            StreamResult sr = (StreamResult) res;
            out = sr.getOutputStream();
            sysId = sr.getSystemId();
            if (out == null) {
                w = sr.getWriter();
            }
            /* Caller owns it, only auto-close if requested to do so:
             * (except that for system-id-only, it'll still be required,
             * see code below)
             */
            requireAutoClose = false;
        }
        else if (res instanceof SAXResult) {
            SAXResult sr = (SAXResult) res;
            sysId = sr.getSystemId();
            if (sysId == null || sysId.length() == 0) {
                throw new XMLStreamException(
                        "Can not create a stream writer for a SAXResult that does not have System Id (support for using SAX input source not implemented)");
            }
            requireAutoClose = true;
        }
        else if (res instanceof DOMResult) {
            return WstxDOMWrappingWriter.createFrom(mConfig.createNonShared(), (DOMResult) res);
        }
        else {
            throw new IllegalArgumentException("Can not instantiate a writer for XML result type " + res.getClass()
                    + " (unrecognized type)");
        }

        if (out != null) {
            return createSW(out, null, encoding, requireAutoClose);
        }
        if (w != null) {
            return createSW(null, w, encoding, requireAutoClose);
        }
        if (sysId != null && sysId.length() > 0) {
            /* 26-Dec-2008, TSa: If we must construct URL from system id,
             *   it means caller will not have access to resulting
             *   stream, thus we will force auto-closing.
             */
            requireAutoClose = true;
            try {
                out = URLUtil.outputStreamFromURL(URLUtil.urlFromSystemId(sysId));
            }
            catch (IOException ioe) {
                throw new WstxIOException(ioe);
            }
            return createSW(out, null, encoding, requireAutoClose);
        }
        throw new XMLStreamException(
                "Can not create Stax writer for passed-in Result -- neither writer, output stream or system id was accessible");
    }
}
