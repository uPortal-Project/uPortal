

import org.apache.xalan.xslt.*;
import org.jasig.portal.*;
import org.xml.sax.*;
import org.apache.xalan.xslt.*;
import org.apache.xml.serialize.*;
import org.xml.sax.helpers.*;


public class StylesheetSetTester {
    public static void main(String[] args)
	{
      // read the imaginary file;
      try{
      System.out.println("start.");
      StylesheetSet set=new StylesheetSet();
      System.out.println("loading properties ...");
      //set.setMediaProps("http://192.168.0.2:8080/portal/media.properties");
      set.setMediaProps("/home/bw/ibs/portal/properties/media.properties");
      System.out.println("parsing the input file");
      Parser channelParser = ParserFactory.makeParser("org.apache.xerces.parsers.SAXParser");

      DocumentHandler pout=new HTMLSerializer(System.out,new OutputFormat("HTML","UTF-8",true));
      channelParser.setDocumentHandler(set);
      set.setDocumentHandler(pout);
      //      channelParser.parse(new org.xml.sax.InputSource(new java.io.FileInputStream("/home/bw/ibs/portal/source/stylesheetlist.xml")));
      channelParser.parse(new org.xml.sax.InputSource(new java.io.FileInputStream("/home/bw/ibs/portal/source/org/jasig/portal/xmlchannels/RSSDocumentChannel_stylesheet_list.xml")));
      //channelParser.parse(new org.xml.sax.InputSource(new java.io.FileInputStream("/home/bw/ibs/portal/source/org/jasig/portal/xmlchannels/RSSDocumentChannel_stylesheet_list.xml")));
      //      XSLTInputSource is=set.getStylesheet("compact");
      //XSLTInputSource bs=set.getStylesheet("normal", "avantgo");
      //XSLTInputSource cs=set.getStylesheet();
      System.out.println("done.");
      } catch (Exception e) {
        System.out.println(e);
        }
	}
}
