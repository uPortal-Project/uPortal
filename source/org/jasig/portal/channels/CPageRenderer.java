package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import com.objectspace.xml.*;
import com.oroinc.text.regex.*;
import org.jasig.portal.*;
import org.jasig.portal.layout.*;

import java.net.*;

/**
 * This is a user-defined channel for rendering a web page.
 * 
 * @author Ken Weiner
 * @version $Revision$
 */
public class CPageRenderer implements org.jasig.portal.IChannel                            
{ 
  protected String m_sUrl = null;
  protected String m_sUrlBaseDir = null;
  protected String m_sUrlServer = null;

  private ChannelConfig chConfig = null;
  
  public void init (ChannelConfig chConfig) {this.chConfig = chConfig;}
  public String getName () {return (String) chConfig.get ("name");}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return false;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}  
  public boolean hasHelp () {return false;}  
  
  public int getDefaultDetachWidth () {return 0;}
  public int getDefaultDetachHeight () {return 0;}
  
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    try 
    {
      m_sUrl = (String) chConfig.get ("url");
      // if the URL has only two slashes, terminate it with a third
      if (m_sUrl.substring(m_sUrl.indexOf("/")+1).lastIndexOf("/") == 
	  m_sUrl.substring(m_sUrl.indexOf("/")+1).indexOf("/"))
	  m_sUrl = new String(m_sUrl + "/");
      m_sUrlBaseDir =
	  new String(m_sUrl.substring(0, m_sUrl.lastIndexOf("/")));
      if (m_sUrlBaseDir.substring(7).indexOf("/") != -1)
	  m_sUrlServer = 
	      new String(m_sUrlBaseDir.substring(7,    // 7 to skip "http://"
	        m_sUrlBaseDir.substring(7).indexOf("/")+7));
      else
	  m_sUrlServer = 
	      new String(m_sUrlBaseDir.substring(7));

      String sLine = null;
      URL url = new URL (m_sUrl);
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      BufferedReader theHTML = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      
      StringBuffer sbHTML = new StringBuffer (1024);
      
      while ((sLine = theHTML.readLine()) != null)
        sbHTML.append (sLine + "\n");
      
      // Filter out HTML between body tags
      String sHTML = grabHtmlBody (sbHTML.toString ());
      
      if (sHTML != null)
      {
        sHTML = replaceRelativeImages (sHTML);
        sHTML = replaceRelativeLinks (sHTML);
	if (chConfig.get("eliminateScript") != null &&
	    chConfig.get("eliminateScript").equals("true"))
	  sHTML = eliminateScript (sHTML);
	if (chConfig.get("portalAware") != null &&
	    chConfig.get("portalAware").equals("true"))
	  sHTML = eliminateIrrelevant (sHTML);
                    
        out.println (sHTML);
      }
      else
      {
        out.println ("<b>" + m_sUrl + "</b> cannot be rendered within a channel. <p><i>Note: Pages containing framesets are not allowed.</i>");
      }
    }
    catch (Exception e)
    {
      try
      {
        out.println ("<b>" + m_sUrl + "</b> is currently unreachable.");
      }
      catch (Exception ex)
      {
        Logger.log (Logger.ERROR, e);
      }
      
      Logger.log (Logger.ERROR, e);
    }
  }
  
  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {    
    // This channel is not editable
  }
  
  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel has no help
  }
  
  // TODO: should be updated to use ORO regex matching instead of
  // simple String operations
  protected String grabHtmlBody (String sHTML)
  {    
    try 
    {
      int iBegin, iEnd;
      
      if (sHTML.indexOf ("<body") >= 0)
      {
        iBegin = sHTML.indexOf ('>', sHTML.indexOf ("<body")) + 1;
        iEnd = sHTML.indexOf ("</body>");
      }
      else
      {
        iBegin = sHTML.indexOf ('>', sHTML.indexOf ("<BODY")) + 1;
        iEnd = sHTML.indexOf ("</BODY>");
      }
      
      return sHTML.substring (iBegin, iEnd);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    return null;
  }
  
  // replace relative SRC attributes in <IMG> tags with absolute references
  protected String replaceRelativeImages (String sHTML)
  {    
    // uses the ORO regular-expression engine (Perl5-style regexes)
    String r1 = "<img\\s+(.*?)src=\"/(.*?)\"(.*?)>";
    String t1 = "<img $1src=\"http://" + m_sUrlServer + "/$2\"$3>";

    String r2 = "<img\\s+(.*?)src=\"([^:]*?)\"(.*?)>";
    String t2 = "<img $1src=\"" + m_sUrlBaseDir + "/$2\"$3>";

    Pattern p1 = null;
    Pattern p2 = null;

    // Create Perl5Compiler and Perl5Matcher instances.
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      // for relative links with full pathname
      p1 = compiler.compile(r1,	Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
      // for truly relative links
      p2 = compiler.compile(r2, Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
    } catch(MalformedPatternException e){
      Logger.log(Logger.ERROR, e);
      return sHTML;
    }

    String o1 = Util.substitute(matcher, p1, t1, sHTML,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    String o2 = Util.substitute(matcher, p2, t2, o1,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    return o2;
  }

  /** like repaceRelativeImages but with HREF instead of SRC... 
   *  coule be integrated later to use the same logic for both */
  protected String replaceRelativeLinks (String sHTML)
  {
    // uses the ORO regular-expression engine (Perl5-style regexes)
    String r1 = "<a\\s+(.*?)href=\"/(.*?)\"(.*?)>";
    String t1 = "<a $1href=\"http://" + m_sUrlServer + "/$2\"$3>";

    String r2 = "<a\\s+(.*?)href=\"([^:]*?)\"(.*?)>";
    String t2 = "<a $1href=\"" + m_sUrlBaseDir + "/$2\"$3>";

    Pattern p1 = null;
    Pattern p2 = null;

    // Create Perl5Compiler and Perl5Matcher instances.
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      // for relative links with full pathname
      p1 = compiler.compile(r1,	Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
      // for truly relative links
      p2 = compiler.compile(r2, Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
    } catch(MalformedPatternException e){
      Logger.log(Logger.ERROR, e);
      return sHTML;
    }
    String o1 = Util.substitute(matcher, p1, t1, sHTML,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    String o2 = Util.substitute(matcher, p2, t2, o1,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    return o2;
  }

  // get rid of the contents of <script> tags
  protected String eliminateScript(String sHTML) {
    // uses the ORO regular-expression engine (Perl5-style regexes)
    String r1 = "<script>.*?</script>";
    String t1 = "<script><!-- Eliminated by UPortal --></script>";

    Pattern p1 = null;

    // Create Perl5Compiler and Perl5Matcher instances.
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      // for relative links with full pathname
      p1 = compiler.compile(r1,	Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
    } catch(MalformedPatternException e){
      Logger.log(Logger.ERROR, e);
      return sHTML;
    }
    String o1 = Util.substitute(matcher, p1, t1, sHTML,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    return o1;
  }

  // get rid of everything that's not inside <portal> tags
  protected String eliminateIrrelevant(String sHTML) {
    // uses the ORO regular-expression engine (Perl5-style regexes)
    String r1 = ".*<portal>";
    String t1 = "";

    String r2 = "</portal>.*";
    String t2 = "";

    Pattern p1 = null;
    Pattern p2 = null;

    // Create Perl5Compiler and Perl5Matcher instances.
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      // for relative links with full pathname
      p1 = compiler.compile(r1,	Perl5Compiler.CASE_INSENSITIVE_MASK
	  | Perl5Compiler.SINGLELINE_MASK);
      p2 = compiler.compile(r2,	Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
    } catch(MalformedPatternException e){
      Logger.log(Logger.ERROR, e);
      return sHTML;
    }
    String o1 = Util.substitute(matcher, p1, t1, sHTML,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    String o2 = Util.substitute(matcher, p2, t2, o1,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    return o2;
  }
}
