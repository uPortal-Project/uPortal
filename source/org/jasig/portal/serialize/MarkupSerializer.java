/* Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.serialize;

import org.jasig.portal.IAnchoringSerializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

public interface MarkupSerializer extends ContentHandler, DocumentHandler, LexicalHandler,
DTDHandler, DeclHandler, DOMSerializer, Serializer, IAnchoringSerializer {

}
