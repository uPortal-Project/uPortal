/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.serialize;

import java.io.IOException;

import org.jasig.portal.IAnchoringSerializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

public interface MarkupSerializer extends ContentHandler, DocumentHandler, LexicalHandler,
DTDHandler, DeclHandler, DOMSerializer, Serializer, IAnchoringSerializer {
    public void flush() throws IOException;
}
