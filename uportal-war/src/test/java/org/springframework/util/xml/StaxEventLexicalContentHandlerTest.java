package org.springframework.util.xml;

import static org.mockito.Mockito.verify;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.util.XMLEventConsumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class StaxEventLexicalContentHandlerTest {

    @Mock XMLEventConsumer consumer;
    @Mock XMLEventFactory factory;
    
    StaxEventLexicalContentHandler handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        handler = new StaxEventLexicalContentHandler(consumer, factory);
    }
    
    @Test
    public void testIncludePublicId() throws SAXException {
        handler.startDTD("html", "-//W3C//DTD HTML 4.01 Transitional//EN", "http://www.w3.org/TR/html4/loose.dtd");
        Mockito.verify(factory).createDTD("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
    }

    @Test
    public void testIncludeSystemId() throws SAXException {
        handler.startDTD("html", null, "http://www.w3.org/TR/html4/loose.dtd");
        Mockito.verify(factory).createDTD("<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/html4/loose.dtd\">");
    }

    @Test
    public void testEmptySystemId() throws SAXException {
        handler.startDTD("html", null, StaxEventLexicalContentHandler.EMPTY_SYSTEM_IDENTIFIER);
        verify(factory).createDTD("<!DOCTYPE html>");
    }

}
