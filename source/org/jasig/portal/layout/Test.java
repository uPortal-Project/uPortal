package org.jasig.portal.layout;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * <p>Title: Test class</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic</p>
 * @author Michael Ivanov
 * @version 1.0
 */

public class Test {


  protected class TestHandler extends DefaultHandler {

   private Writer out;


   public TestHandler() {
    super();
    try {
     out =  new BufferedWriter(new OutputStreamWriter(System.out));
    } catch ( Exception e ) { }
   }

    private String indentString = "    "; // Amount to indent
    private int indentLevel = 0;

    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================

    public void startDocument()
    throws SAXException
    {
        nl();
        nl();
        emit("START DOCUMENT");
        nl();
        emit("<?xml version='1.0' encoding='UTF-8'?>");
    }

    public void endDocument()
    throws SAXException
    {
        nl(); emit("END DOCUMENT");
        try {
            nl();
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    public void startElement(String namespaceURI,
                             String lName, // local name
                             String qName, // qualified name
                             Attributes attrs)
    throws SAXException
    {
        indentLevel++;
        nl(); emit("ELEMENT: ");
        String eName = lName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false
        emit("<"+eName);
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name
                if ("".equals(aName)) aName = attrs.getQName(i);
                nl();
                emit("   ATTR: ");
                emit((aName!=null)?aName:"**");
                emit("\t\"");
                emit((attrs.getValue(i)!=null)?attrs.getValue(i):"**");
                emit("\"");
            }
        }
        if (attrs != null && attrs.getLength() > 0) nl();
        emit(">");
    }

    public void endElement(String namespaceURI,
                           String sName, // simple name
                           String qName  // qualified name
                          )
    throws SAXException
    {
        nl();
        emit("END_ELM: ");
        emit("</"+sName+">");
        indentLevel--;
    }

    public void characters(char buf[], int offset, int len)
    throws SAXException
    {
        nl(); emit("CHARS:   ");
        String s = new String(buf, offset, len);
        if (!s.trim().equals("")) emit(s);
    }

    //===========================================================
    // Utility Methods ...
    //===========================================================

    // Wrap I/O exceptions in SAX exceptions, to
    // suit handler signature requirements
    private void emit(String s)
    throws SAXException
    {
        try {
            out.write(s);
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    // Start a new line
    // and indent the next line appropriately
    private void nl()
    throws SAXException
    {
        String lineEnd =  System.getProperty("line.separator");
        try {
            out.write(lineEnd);
            for (int i=0; i < indentLevel; i++) out.write(indentString);
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }
}




  private class DataStruct {
    private int a = 2;
    private String b = "init";

    public void setA( int a ) {
       this.a = a;
    }

    public void setB( String b ) {
       this.b = b;
    }

    public int getA() {
       return a;
    }

    public String getB() {
       return b;
    }
  }

  public Test() {
    try {

     IPerson person = (org.jasig.portal.security.provider.PersonImpl)
           Class.forName("org.jasig.portal.security.provider.PersonImpl").newInstance();

     person.setID(2);

     IUserLayoutStore layoutStore =  UserLayoutStoreFactory.getUserLayoutStoreImpl();
     Document layoutDOM = layoutStore.getUserLayout(person,layoutStore.getUserProfileById(person,1));
     //System.out.println( "The string representation of the DOM is : \n" + layoutDOM );

     System.out.println( "Getting the internal layout representation from the UserLayout manager....");
     Hashtable layout = ((IAggregatedUserLayoutStore)layoutStore).getAggregatedUserLayout(person,layoutStore.getUserProfileById(person,1));
     //System.out.println( "The string representation of the AggregatedUserLayout is : \n" + layout );

     //Instantiate UserLayoutManager implementation
     AggregatedUserLayoutImpl layoutManager = new AggregatedUserLayoutImpl(person, 1 /*layoutId*/, (IAggregatedUserLayoutStore)layoutStore );
     System.out.println("Setting DOM layout to the UserLayout manager....");
     layoutManager.setUserLayoutDOM(layoutDOM);
     //System.out.println("Setting the internal layout representation to the UserLayout manager....");
     //layoutManager.setUserLayout(layout);


          // Use an instance of ourselves as the SAX event handler
          DefaultHandler handler = new TestHandler();
          // Use the default (non-validating) parser
          SAXParserFactory factory = SAXParserFactory.newInstance();
        try {

            // Parse the input
            SAXParser saxParser = factory.newSAXParser();
            System.out.println("Filling the ContentHandler by the UserLayout data...");
            layoutManager.getUserLayout(handler);
            //System.out.println("Parsing the ContentHandler using the SAX Parser...");

        } catch (Throwable t) {
            t.printStackTrace();
          }

     } catch ( Exception e ) {
        System.out.println ( "Test Exception: " + e );
       }


    /* Hashtable hash = new Hashtable();
     DataStruct data = new DataStruct();
     hash.put("1",data);
     System.out.println( "Init values: a=" + data.getA() + " b=" + data.getB() );
     data.setA(20);
     data.setB("changed");
     DataStruct data1 = (DataStruct) hash.get("1");
     System.out.println( "Changed values: a=" + data1.getA() + " b=" + data1.getB() );
     data1.setA(99);
     data1.setB("changed_changed");
     DataStruct data2 = (DataStruct) hash.get("1");
     System.out.println( "Changed Changed values: a=" + data2.getA() + " b=" + data2.getB() );
     */
  }


  public static void main(String[] args) {


    Test test1 = new Test();

  }
}